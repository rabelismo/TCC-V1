import ReactMarkdown, { type Components } from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";
import "highlight.js/styles/github-dark.css";

interface TheoryPanelProps {
  markdown: string;
}

// O <h1> da página é o título do módulo; os títulos do markdown descem um nível.
const components: Components = {
  h1: ({ children, id }) => <h2 className="theory-title" id={id}>{children}</h2>,
  h2: ({ children, id }) => <h3 id={id}>{children}</h3>,
  h3: ({ children, id }) => <h4 id={id}>{children}</h4>,
  img: ({ src, alt, title }) => (
    <img src={src} alt={alt ?? ""} title={title} loading="lazy" decoding="async" />
  ),
};

export default function TheoryPanel({ markdown }: TheoryPanelProps) {
  return (
    <article className="theory-panel">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeHighlight]}
        components={components}
      >
        {markdown}
      </ReactMarkdown>
    </article>
  );
}
