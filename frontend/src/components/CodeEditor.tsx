import Editor, { type BeforeMount } from "@monaco-editor/react";

interface CodeEditorProps {
  code: string;
  onChange: (value: string) => void;
}

const THEME = "gdbl-dark";

// Tema alinhado aos tokens de src/index.css (--bg-sunken, --accent etc.)
const defineTheme: BeforeMount = (monaco) => {
  monaco.editor.defineTheme(THEME, {
    base: "vs-dark",
    inherit: true,
    rules: [
      { token: "comment", foreground: "6b6c74", fontStyle: "italic" },
      { token: "keyword", foreground: "e2a54f" },
      { token: "string", foreground: "8fcfa8" },
      { token: "number", foreground: "d9b98a" },
    ],
    colors: {
      "editor.background": "#0a0b0c",
      "editor.foreground": "#ececee",
      "editor.lineHighlightBackground": "#131417",
      "editor.lineHighlightBorder": "#00000000",
      "editorLineNumber.foreground": "#3f4047",
      "editorLineNumber.activeForeground": "#a4a5ac",
      "editorCursor.foreground": "#e2a54f",
      "editor.selectionBackground": "#e2a54f33",
      "editorIndentGuide.background1": "#1b1c20",
      "editorWidget.background": "#131417",
      "editorWidget.border": "#33343a",
      "scrollbarSlider.background": "#33343a80",
    },
  });
};

export default function CodeEditor({ code, onChange }: CodeEditorProps) {
  return (
    <div className="code-editor">
      <Editor
        height="100%"
        language="python"
        theme={THEME}
        beforeMount={defineTheme}
        value={code}
        onChange={(value) => onChange(value ?? "")}
        options={{
          ariaLabel: "Editor de código Python. Pressione Ctrl+M para sair do editor com Tab.",
          minimap: { enabled: false },
          fontSize: 14,
          fontFamily: "'Geist Mono', ui-monospace, 'Cascadia Code', monospace",
          fontLigatures: true,
          lineHeight: 22,
          lineNumbers: "on",
          renderLineHighlight: "all",
          scrollBeyondLastLine: false,
          automaticLayout: true,
          tabSize: 4,
          wordWrap: "on",
          padding: { top: 14, bottom: 14 },
          smoothScrolling: true,
          cursorBlinking: "smooth",
          overviewRulerBorder: false,
        }}
      />
    </div>
  );
}
