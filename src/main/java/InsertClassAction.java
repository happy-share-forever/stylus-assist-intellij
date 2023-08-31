import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.psi.PsiFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class InsertClassAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        if (anActionEvent.getProject() == null) return;
        //获取当前的编辑器
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);

        //获取当前的文件
        PsiFile currentPsiFile = anActionEvent.getData(LangDataKeys.PSI_FILE);
        if (currentPsiFile == null) return;

        //分割文件名 获取文件名
        String currentFileName = currentPsiFile.getName().split("\\.")[0];

        //获取当前选择的文本
        final SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();

        //获取当前全文
        String allContent = editor.getDocument().getText();

        if (selectedText == null) {
            // 获取光标位置检测前后是非为""
            int startIndex = selectionModel.getSelectionStart();
            int endIndex = selectionModel.getSelectionEnd();
            System.out.printf("start: %d, end: %d\n", startIndex, endIndex);
            if (startIndex == endIndex && startIndex > 0) {
                char beforeChar = allContent.charAt(startIndex -1);
                char afterChar = allContent.charAt(startIndex);
                System.out.printf("before: %c, after: %c\n", beforeChar, afterChar);
                if (beforeChar == '"' && afterChar == '"') {
                    insertString(editor, startIndex, "$style.");
                    moveCaret(editor, startIndex + 7);
                    return;
                } else if (beforeChar == '.' && afterChar == '"') {
                    // 此处应该终止执行
                    return;
                }
            }
            // 如果不是
            selectionModel.selectWordAtCaret(true);
            selectedText = selectionModel.getSelectedText();
            if ("$style".equals(selectedText) || "\"".equals(selectedText)) {
                return;
            }
        }

        String insertClass = "." + Utils.formatClass(selectedText);
        String preInsertString = "\n" + insertClass + "\n  \n";
        Document document = editor.getDocument();
        int endTag = allContent.indexOf("</style>");
        if (endTag == -1) {
            endTag = allContent.length() + 21;
            insertString(editor, allContent.length(), "<style lang=\"stylus\"></style>");
        } else {
            Pattern pattern = Pattern.compile("<style[\\s\\S]*>([\\s\\S]*?)</style>");
            Matcher matcher = pattern.matcher(allContent);
            if (matcher.find() && matcher.group(1).trim().length() <= 0) {
                preInsertString = preInsertString.substring(1);
            }
        }
        int newEndTag = endTag;
        String insertString = preInsertString;
        insertString(editor, newEndTag, insertString);

        assert currentPsiFile.getParent() != null;
        PsiFile targetPsiFile = currentPsiFile.getParent().findFile(currentFileName + ".vue");
        assert targetPsiFile != null;
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(
                anActionEvent.getProject(),
                targetPsiFile.getVirtualFile(),
                document.getLineNumber(endTag - 1));
        Editor targetEdit = FileEditorManager.getInstance(anActionEvent.getProject())
                .openTextEditor(openFileDescriptor, true);
        assert targetEdit != null;
        ScrollingModel scrollingModel = targetEdit.getScrollingModel();
        scrollingModel.scrollTo(targetEdit.offsetToLogicalPosition(endTag + insertString.length() - 1), ScrollType.MAKE_VISIBLE);
        moveCaret(targetEdit, endTag + insertString.length() - 1);
    }

    /**
     * 在原位置插入字符串
     */
    private void insertString(Editor editor, int newEndTag, String insertString) {
        Document document = editor.getDocument();
        WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> document.insertString(newEndTag, insertString));
    }

    /**
     * 移动光标到指定位置
     */
    private void moveCaret(Editor editor, int newEndTag) {
        CaretModel caretModel = editor.getCaretModel();
        caretModel.moveToOffset(newEndTag);
    }
}
