export const conceptIcons: Record<string, string> = {
  STRINGS: "\" \"",
  INPUT_VARIABLES: "x =",
  ARRAYS_1D: "[ ]",
  ARRAYS_2D: "[[ ]]",
  FUNCTIONS: "def",
  STATE_MUTATION: ":=",
  TYPE_CASTING: "int",
  IF_ELSE: "if",
  LOGIC_OPERATORS: "&&",
  WHILE_LOOP: "whl",
  INPUT_VALIDATION: "try",
  AND_OPERATOR: "and",
  FOR_LOOP: "for",
  BREAK_STATEMENT: "brk",
  VARIABLES: "x = 1",
  ARRAYS: "[ ]",
  CONTROL_FLOW: "if",
  LOOPS: "for",
};

export interface BlockDef {
  label: string;
  range: [number, number];
}

export const blocks: BlockDef[] = [
  { label: "O Básico da Comunicação", range: [1, 2] },
  { label: "Desenhando o Campo de Batalha", range: [3, 5] },
  { label: "Interação e Estado", range: [6, 7] },
  { label: "O Juiz da Partida", range: [8, 9] },
  { label: "O Motor do Jogo", range: [10, 11] },
  { label: "O Algoritmo de Vitória", range: [12, 14] },
];

export function getBlockForModule(orderIndex: number): BlockDef | undefined {
  return blocks.find(
    (b) => orderIndex >= b.range[0] && orderIndex <= b.range[1]
  );
}
