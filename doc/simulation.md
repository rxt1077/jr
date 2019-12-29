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

<div id="vis1" style="width: 75%; height: 500px;"></div>
<script type="text/javascript">
    var yourVlSpec = {
    	"title": "Extended Network Growth",
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
    vegaEmbed('#vis1', yourVlSpec);
</script>

The way the extended network grows over time appears to be a sigmoid function.
Using R we can fit a function to it:

```R
# Simple R script to fit a sigmoid and write a new column of fitted data to
# a CSV

sigmoid = function(params, x) {
	A <- params[1] # the lower asymptote
	K <- params[2] # the upper asymptote
	B <- params[3] # the growth rate
	M <- params[4] # the time of maximum growth

	A + (K - A) / (1 + exp(-B*(x-M)))
}

data = read.csv("extended_setup.csv")
x <- data$iterations
y1 <- data$avg_extended_size
fitmodel <- nls(y1 ~ A + (K - A) / (1 + exp(-B*(x - M))),
		start=list(A=10,K=100,B=0.01,M=500))
print(fitmodel)
params = coef(fitmodel)
y2 <- sigmoid(params, x)
#plot(x, y1, col='blue')
#points(x, y2, col='red')
output_data <- data.frame(iterations=x, avg_extended_size=y1, fit=y2)
write.csv(output_data, "extended_setup_fit.csv", row.names=FALSE)
```

The output from R shows that we are getting a relatively close fit:

```R
> source("extended_setup.R")
Nonlinear regression model
  model: y1 ~ A + (K - A)/(1 + exp(-B * (x - M)))
   data: parent.frame()
        A         K         B         M
 12.69036 102.77688   0.01057 542.87158
 residual sum-of-squares: 2307

Number of iterations to convergence: 7
Achieved convergence tolerance: 1.43e-06
```

<div id="vis2" style="width: 75%; height: 500px;"></div>
<script type="text/javascript">
    var yourVlSpec = {
    	"title": "Extended Network Growth with Fitted Sigmoid",
        "data": {
            "url": "https://raw.githubusercontent.com/rxt1077/jr/master/data/extended_setup_fit.csv"
        },
        "width": "container",
        "height": "container",
        "layer": [
            {
                "mark": "line",
                "encoding": {
                    "x": {
                        "field": "iterations",
                        "type": "quantitative",
                        "axis": {"title": "Number of Random Sync Events"}
                    },
                    "y": {
                        "field": "fit",
                        "type": "quantitative",
                        "axis": {"title": "Average Size of Extended Network"}
                    },
		    "color": {"value": "red"}
                }
            },
            {
                "mark": "line",
                "encoding": {
                    "x": {
                        "field": "iterations",
                        "type": "quantitative",
                    },
                    "y": {
                        "field": "avg_extended_size",
                        "type": "quantitative",
                    }
                }
            }
        ]
    };
    vegaEmbed('#vis2', yourVlSpec);
</script>

It appears the average number of nodes in an extended network as a function of
random sync events can be expressed as:

$$ f(x) = f + \frac{f(n-1)}{1+e^{-B*(x-M)}} $$

Where n is the total number of nodes, f is the number of nodes we follow to
start, B is the growth rate and M is the iteration at which we experience the
the most growth.
