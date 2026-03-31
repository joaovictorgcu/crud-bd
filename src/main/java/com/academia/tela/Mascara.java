package com.academia.tela;

import javax.swing.*;
import javax.swing.text.*;

public class Mascara {

    public static void cpf(JTextField campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FormatFilter("###.###.###-##"));
    }

    public static void telefone(JTextField campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FormatFilter("(##) #####-####"));
    }

    public static void data(JTextField campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FormatFilter("##/##/####"));
    }

    public static void cep(JTextField campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FormatFilter("#####-###"));
    }

    private static class FormatFilter extends DocumentFilter {
        private final String mascara;

        FormatFilter(String mascara) {
            this.mascara = mascara;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, text);
            String resultado = aplicar(soNumeros(sb.toString()));
            fb.remove(0, fb.getDocument().getLength());
            super.insertString(fb, 0, resultado, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, text != null ? text : "");
            String resultado = aplicar(soNumeros(sb.toString()));
            fb.remove(0, fb.getDocument().getLength());
            super.insertString(fb, 0, resultado, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.delete(offset, offset + length);
            String resultado = aplicar(soNumeros(sb.toString()));
            fb.remove(0, fb.getDocument().getLength());
            super.insertString(fb, 0, resultado, null);
        }

        private String soNumeros(String texto) {
            return texto.replaceAll("[^0-9]", "");
        }

        private String aplicar(String numeros) {
            StringBuilder resultado = new StringBuilder();
            int idx = 0;
            for (int i = 0; i < mascara.length() && idx < numeros.length(); i++) {
                char m = mascara.charAt(i);
                if (m == '#') {
                    resultado.append(numeros.charAt(idx++));
                } else {
                    resultado.append(m);
                    if (i < mascara.length() - 1 && mascara.charAt(i + 1) == '#') {
                        // proximo eh digito, continua
                    }
                }
            }
            return resultado.toString();
        }
    }
}
