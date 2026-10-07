/* =====================================================================
   Vibe Barbearia — tema.js
   Carregado no <head> para aplicar o tema (escuro/claro) ANTES de
   desenhar a página, evitando o "piscar" de cores. O padrão é o tema
   escuro da marca; a escolha do usuário fica salva no navegador.
   ===================================================================== */
(function () {
    'use strict';
    var tema = null;
    try { tema = localStorage.getItem('vibe.tema'); } catch (e) { /* sem armazenamento */ }
    if (tema !== 'claro' && tema !== 'escuro') tema = 'escuro';
    document.documentElement.setAttribute('data-tema', tema);
})();
