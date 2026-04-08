# Fortuna To-Do List

### 📝 Data Entry

* Secondary Emoji? using '-' separators?

### 🔮 Data Access

* Nostalgia: to remember what you did last year(s) this day! (in a NostalgiaDialog?)
* Index `Luna`s as serialized in cache
* Make diagrams out of search results in web

### 🚀 UI

* `VariabilisDialog` gets deformed when another app is in the pop-up mode above it
  I call `show()` on `VariabilisDialog` inside `onCreateDialog()`; is it causing the problem?
  I did the same in `ConvertDialog`
* Make white areas of the background more silver-cyan
* A shutdown button inside web
* Server status doesn't survive `Main`'s restart
* An icon for `ConvertDialog`

### ✨ Special

* Send a broadcast with action `ir.mahdiparastesh.fortuna.NYX` which any app can receive
* emojis are not reconstructed from unicode in `Server/save`
