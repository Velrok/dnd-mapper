# D&D Mapper

A work in progress online tool for remote or digital dungeons and dragons groups.

It overlays a grid over any image, which can serve as the map. Players and moster tokens can be added by the GM and players can see the movement of tokens on the map.


## Dev setup

The backend is written in clojure the frontend in clojure script via `shadow-cljs`

 - install [Leiningen](https://leiningen.org) `brew install leiningen`
 - install node `brew install node`
 - install node packages `npm install`


 - start the backend `lein run`
    - or run `lein repl` and eval `(mount/start)` in the `dnd-mapper.server.main` namespace
 - start the clojurescript compilation pipeline via `shadow-cljs watch --debug app`
