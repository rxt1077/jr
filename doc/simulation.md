---
title: jr Network Simulations
header-includes:
- <script src="https://cdn.jsdelivr.net/npm/vega@5"></script>
- <script src="https://cdn.jsdelivr.net/npm/vega-lite@4"></script>
- <script src="https://cdn.jsdelivr.net/npm/vega-embed@6"></script>
...

jr was primarily built as a simulation platform. `simulation.clj` has many
interesting functions for building / analyzing jr networks and monitoring
their growth.

## Extended Network

### Testing the Extended Network Functionality

This is our initial setup with private keys replaced with nil and public keys
replaced with the names Alice, Bob, and Carol.

```clojure
({:public "Alice",
  :private nil,
  :messages #{},
  :following #{},
  :extended #{}}

 {:public "Bob",
  :private nil,
  :messages #{},
  :following #{},
  :extended #{}}

 {:public "Carol",
  :private nil,
  :messages #{},
  :following #{},
  :extended #{}})
```

As you can see everyone has an empty message set, is not following anyone,
and does not have anyone in their extended network.

Alice decides to follow Bob and Bob decides the follow Carol. This leaves us
with the following network state:

```clojure
({:public "Alice",
  :private nil,
  :messages
  #{{:signature nil,
     :public "Alice",
     :following "Bob",
     :timestamp 1576107325835}},
  :following #{"Bob"},
  :extended #{"Bob"}}

 {:public "Bob",
  :private nil,
  :messages
  #{{:signature nil,
     :public "Bob",
     :following "Carol",
     :timestamp 1576107325846}},
  :following #{"Carol"},
  :extended #{"Carol"}}

 {:public "Carol",
  :private nil,
  :messages #{},
  :following #{},
  :extended #{}})
```

As you can see Bob is now in Alice's `following network` and her
`extended network` Alice has created a message saying she is following Bob so
that when people sync with her they can build their own `extended network` and
collect messages for the "friends of their friends."

Carol is now in Bob's `following network` and `extended network`. He has also
created a message saying he is following Carol.

Now to test the functionality of the `extended network` lets have Alice sync
with Bob.

```clojure
({:public "Alice",
  :private nil,
  :messages
  #{{:signature nil,
     :public "Bob",
     :following "Carol",
     :timestamp 1576107325846}
    {:signature nil,
     :public "Alice",
     :following "Bob",
     :timestamp 1576107325835}},
  :following #{"Bob"},
  :extended #{"Carol" "Bob"}}

 {:public "Bob",
  :private nil,
  :messages
  #{{:signature nil,
     :public "Bob",
     :following "Carol",
     :timestamp 1576107325846}},
  :following #{"Carol"},
  :extended #{"Carol"}}

 {:public "Carol",
  :private nil,
  :messages #{},
  :following #{},
  :extended #{}})
```

Alice now has two messages: a following message she created and Bob's message
saying he was following Carol. By going through her messages, Alice was able
to update her `extended network` to include Carol.

### Extended Network Setup

`sim/net-bootstrap n f` will create a network of n nodes each following f random
nodes. Using this we can build a network of 100 nodes, each following 10 others
and use `sim/print-stats` to see how it was set up:

```clojure
jr.core=> (def nodes (sim/net-boostrap 100 10))
#'jr.core/nodes
jr.core=> (sim/print-stats nodes)
=== 100 nodes ===
Averages
  :following 10.00
  :extended  10.00
  :messages  10.00
nil
```

As expected, the extended network of each node is limited to only nodes that a
node is following. Nodes build their following network by finding out about
followers in messages that they sync. `sim/rand-sync` will pick two nodes at
random and have them sync with eachother. We can use it to watch how the average
extended network grows as nodes randomly sync. `core/extended-propagation-test`
does exactly that, outputing the average size of an extended network to
"data/extended_network_setup.csv" for every iteration:

Running it and graphing the output yields:

<div id="vis" style="width: 75%; height: 500px;"></div>
<script type="text/javascript">
    var yourVlSpec = {
        "data": {
            "url": "https://raw.githubusercontent.com/rxt1077/jr/master/data/extended_setup.csv"
        },
        "width": "container",
        "height": "container",
        "mark": "line",
        "encoding": {
            "x": {
                "field": "iterations",
                "type": "quantitative",
                "axis": {"title": "Number of Random Sync Events"}
            },
            "y": {
                "field": "avg_extended_size",
                "type": "quantitative",
                "axis": {"title": "Average Size of Extended Network"}
            }
        }
    };
    vegaEmbed('#vis', yourVlSpec);
</script>
