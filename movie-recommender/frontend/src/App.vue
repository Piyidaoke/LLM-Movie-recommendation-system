<template>
  <router-view />
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;500;600;700;800&family=Instrument+Sans:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap');

:root {
  --bg-void: #0d0c11;
  --bg-film: #121118;
  --bg-frame: #181722;
  --bg-cell: #1f1e2b;
  --film-gold: #dba148;
  --film-amber: #c4883a;
  --film-warm: rgba(219,161,72,0.08);
  --ai-blue: #56c8d8;
  --ai-glow: rgba(86,200,216,0.12);
  --ai-line: rgba(86,200,216,0.25);
  --text-1: #ece6d9;
  --text-2: #8c8696;
  --text-3: #504b5e;
  --border: rgba(219,161,72,0.07);
  --border-bright: rgba(219,161,72,0.18);
  --sprocket: rgba(219,161,72,0.1);
}

*,*::before,*::after{box-sizing:border-box;margin:0;padding:0}

body {
  font-family: 'Instrument Sans', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  background: var(--bg-void);
  color: var(--text-1);
  overflow: hidden;
}
h1,h2,h3,h4,h5,h6 { font-family: 'Syne', sans-serif; letter-spacing: -0.02em; }

::-webkit-scrollbar { width: 3px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: var(--film-amber); border-radius: 2px; opacity: 0.4; }
::selection { background: rgba(219,161,72,0.22); }
</style>
