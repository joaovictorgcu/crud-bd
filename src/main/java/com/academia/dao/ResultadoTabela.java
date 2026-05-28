package com.academia.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado generico de uma consulta: nomes das colunas + linhas.
 * Usado pelas telas de Consultas/Views e pelo Dashboard para exibir
 * qualquer SELECT sem precisar de uma classe de modelo dedicada.
 */
public class ResultadoTabela {

    private final String[] colunas;
    private final List<Object[]> linhas = new ArrayList<>();

    public ResultadoTabela(String[] colunas) {
        this.colunas = colunas;
    }

    public void addLinha(Object[] linha) {
        linhas.add(linha);
    }

    public String[] getColunas() {
        return colunas;
    }

    public List<Object[]> getLinhas() {
        return linhas;
    }

    public int getQtdLinhas() {
        return linhas.size();
    }

    public int getQtdColunas() {
        return colunas.length;
    }

    /** Indice da coluna pelo nome (case-insensitive); -1 se nao existir. */
    public int indiceColuna(String nome) {
        for (int i = 0; i < colunas.length; i++) {
            if (colunas[i].equalsIgnoreCase(nome)) return i;
        }
        return -1;
    }

    /** Valor de uma celula como double (Number ou String numerica). */
    public double comoDouble(int linha, int coluna) {
        Object v = linhas.get(linha)[coluna];
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v != null) {
            try { return Double.parseDouble(v.toString().trim()); } catch (NumberFormatException ignored) {}
        }
        return 0d;
    }

    /** Soma de uma coluna numerica (para indicadores). */
    public double somar(int coluna) {
        double total = 0;
        for (Object[] l : linhas) {
            Object v = l[coluna];
            if (v instanceof Number) total += ((Number) v).doubleValue();
        }
        return total;
    }
}
