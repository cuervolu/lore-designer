import type { DocumentRecord, LoreTypeTag, WorkspaceNode } from "@/types/editor";

function flattenWorkspaceNode(node: WorkspaceNode, paths: string[]) {
  if (node.kind === "directory") {
    paths.push(`${node.path}/`);
  } else {
    paths.push(node.path);
  }

  node.children?.forEach((child) => flattenWorkspaceNode(child, paths));
}

export function mapWorkspaceToTreesData(nodes: WorkspaceNode[]) {
  const paths: string[] = [];
  nodes.forEach((node) => flattenWorkspaceNode(node, paths));
  return paths;
}

export function getTypeTone(type: LoreTypeTag) {
  return `entity-type--${type}`;
}

export function isDocumentPath(
  path: string,
  documents: Record<string, DocumentRecord>,
): path is keyof typeof documents {
  return path in documents;
}

export function formatFieldValue(value: number | string | string[]) {
  if (Array.isArray(value)) {
    return value.join(", ");
  }

  return String(value);
}
