import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import ModuleSidebar from '../components/ModuleSidebar';
import type { Module } from '../types';

const mockModules: Module[] = [
  {
    id: 1,
    orderIndex: 1,
    title: 'Hello, World!',
    description: 'Seu primeiro programa',
    theoryMarkdown: '# Teoria',
    starterCode: "print('hello')",
    expectedOutput: 'hello',
    concept: 'STRINGS',
  },
  {
    id: 2,
    orderIndex: 2,
    title: 'Variáveis e Input',
    description: 'Lendo dados do usuário',
    theoryMarkdown: '# Teoria 2',
    starterCode: 'x = input()',
    expectedOutput: '',
    concept: 'INPUT_VARIABLES',
  },
  {
    id: 3,
    orderIndex: 3,
    title: 'Listas 1D',
    description: 'Array simples',
    theoryMarkdown: '# Teoria 3',
    starterCode: 'arr = []',
    expectedOutput: '',
    concept: 'ARRAYS_1D',
  },
];

describe('ModuleSidebar', () => {
  it('renderiza todos os títulos dos módulos', () => {
    render(
      <ModuleSidebar
        modules={mockModules}
        activeModuleId={null}
        onSelectModule={() => {}}
        completedModuleIds={new Set()}
      />
    );

    expect(screen.getByText('Hello, World!')).toBeInTheDocument();
    expect(screen.getByText('Variáveis e Input')).toBeInTheDocument();
    expect(screen.getByText('Listas 1D')).toBeInTheDocument();
  });

  it('aplica classe "active" no módulo selecionado', () => {
    render(
      <ModuleSidebar
        modules={mockModules}
        activeModuleId={2}
        onSelectModule={() => {}}
        completedModuleIds={new Set()}
      />
    );

    const buttons = screen.getAllByRole('button');
    const activeButton = buttons.find((b) =>
      b.textContent?.includes('Variáveis e Input')
    );
    expect(activeButton).toHaveClass('active');
  });

  it('chama onSelectModule ao clicar em um módulo', async () => {
    const user = userEvent.setup();
    const onSelect = vi.fn();

    render(
      <ModuleSidebar
        modules={mockModules}
        activeModuleId={null}
        onSelectModule={onSelect}
        completedModuleIds={new Set()}
      />
    );

    await user.click(screen.getByText('Listas 1D'));
    expect(onSelect).toHaveBeenCalledWith(3);
  });

  it('exibe barra de progresso com contagem correta', () => {
    render(
      <ModuleSidebar
        modules={mockModules}
        activeModuleId={null}
        onSelectModule={() => {}}
        completedModuleIds={new Set([1, 3])}
      />
    );

    expect(screen.getByText('2/3 concluídos')).toBeInTheDocument();
  });

  it('mostra check icon para módulos concluídos', () => {
    render(
      <ModuleSidebar
        modules={mockModules}
        activeModuleId={null}
        onSelectModule={() => {}}
        completedModuleIds={new Set([1])}
      />
    );

    const buttons = screen.getAllByRole('button');
    const completedButton = buttons.find((b) =>
      b.textContent?.includes('Hello, World!')
    );
    expect(completedButton).toHaveClass('completed');
  });

  it('exibe cabeçalhos de bloco', () => {
    render(
      <ModuleSidebar
        modules={mockModules}
        activeModuleId={null}
        onSelectModule={() => {}}
        completedModuleIds={new Set()}
      />
    );

    expect(screen.getByText('O Básico da Comunicação')).toBeInTheDocument();
    expect(screen.getByText('Desenhando o Campo de Batalha')).toBeInTheDocument();
  });
});
