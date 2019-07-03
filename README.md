# Visualize ABS Traces

This program visualizes traces from ABS models, produced by the ABS compiler
found at: https://github.com/larstvei/abstools.

## Installation

Follow instructions at https://github.com/larstvei/abstools to install the
compiler frontend in order to run models that produce traces for visualization.


Install Leiningen from https://leiningen.org/ (or using your package manager).

To compile a standalone jar, run the following command:

    $ lein uberjar

## Usage

The visualization can be executed with after compiling a standalone jar like so:

    $ java -jar target/visualize-traces-clj-0.1.0-SNAPSHOT-standalone.jar 

To compile an ABS model with:

    $ bin/bash/absc --erlang [options] <absfiles>

Then run the model with the Model API enabled:

    $ gen/erl/run -p 8080

Now press `l` in the visualization. Here is a full list of keyboard commands:

| Key   | Function                          |
|------ |---------------------------------- |
| l     | Load trace from running simulator |
| down  | Scroll down                       |
| up    | Scroll up                         |
| -     | Show fewer events                 |
| +     | Show more events                  |
| c     | Advance time in running model     |
| d     | Fetch all traces from database    |
| left  | Next loaded trace                 |
| right | Previous loaded trace             |
| s     | Save current view to .png         |

## License

Copyright © 2019 Lars Tveito

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
