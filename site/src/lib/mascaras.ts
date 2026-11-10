/** Mascaras de formatação "enquanto digita" para campos de telefone (BR). */

/**
 * Formata um telefone brasileiro conforme a quantidade de dígitos digitados:
 * fixo "(61) 0000-0000" (10 dígitos) ou celular "(61) 90000-0000" (11 dígitos).
 * Não dá pra saber qual formato até o usuário terminar de digitar, então o
 * traço "pula" de posição ao digitar o 11º dígito — é o comportamento padrão
 * de qualquer máscara de telefone brasileira.
 */
export function mascararTelefone(valor: string): string {
  const digitos = valor.replace(/\D/g, "").slice(0, 11);
  if (digitos.length === 0) return "";
  if (digitos.length <= 2) return `(${digitos}`;

  const ddd = digitos.slice(0, 2);
  const resto = digitos.slice(2);
  const tamanhoPrefixo = digitos.length > 10 ? 5 : 4;
  const prefixo = resto.slice(0, tamanhoPrefixo);
  const sufixo = resto.slice(tamanhoPrefixo);

  return sufixo ? `(${ddd}) ${prefixo}-${sufixo}` : `(${ddd}) ${prefixo}`;
}
