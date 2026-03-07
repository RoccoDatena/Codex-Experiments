# Snake Field (Angular)

Classic Snake game sviluppato con Angular + TypeScript.

## Features
- Movimento a griglia con Arrow Keys e WASD
- Crescita del serpente quando mangia il food
- Collisione con bordi/corpo e game over
- Pause/Resume con `Space` o pulsante
- Restart rapido
- Best score persistente (`localStorage`)
- Controlli on-screen per mobile

## Stack
- Angular 20
- TypeScript
- HTML + CSS

## Avvio locale
```bash
npm install
npm run start
```
Apri `http://localhost:4200`.

## Test e build
```bash
npm run test -- --watch=false --browsers=ChromeHeadless
npm run build
```

## Controlli
- `Arrow Keys` / `WASD` per direzione
- `Space` per pausa/ripresa
- `Restart` per nuova partita

## Checklist pre-pubblicazione
- Avvio app locale OK
- Test unitari OK
- Build produzione OK
- README aggiornato
- Nome repository e descrizione GitHub impostati