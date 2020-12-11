# Introduction

*TransitRouter* is a web application for generating shape files of GTFS feeds using a map matching approach described in the paper [Hidden Markov Map Matching Through Noise and Sparseness](https://www.ismll.uni-hildesheim.de/lehre/semSpatial-10s/script/6.pdf).

*TransitRouter* uses the OSM routing engine [GraphHopper]() and a modified version the [GraphHopper Map Matching library](). 

## Limitations of the GraphHopper Map Matching library
- removes stations that are to close
- no turn restrictions / costs
- no support for inter hop turns restrictions
- no directed candidates


## Our map matching approach
Given a trip $T$  with a ordered sequence of stations $S = (s_0, s_1, s_2, ..., s_n)$ we want to find its path $P$ through our street network graph $G=(V, E)$.

### Finding node candidates
The GPS positions of our stations might not be accurate so we cannot use them directly to find our path. Instead we create a set $C_i \subseteq V$ of candidate nodes for every stations $s_i$.
For every edge $e \in E$ within distance $r$ around $s_i$ we add the projection of $s_i$ on $e$ to $C_i$.

Now that we have several node candidates for every station we need to find the most likely sequence of node candidates. We find this sequence by using a Hidden Markov Model (*HMM*).

### Hidden Markov Model *HMM*
For our *HMM* we use the stations $s_i$ as observations and the candidate nodes $C_i$ as hidden states. The most likely sequence is computed via the Viterbi algorithm.

#### Emission probability
For the emission probability we use the great circle distance $d$ between the station $s_i$ and its candidate node $c^i_k$ and apply a weighting function with the tuning parameter $\sigma$.

$$d=\|s_i - c_i^k\|_{\text{great circle}}$$
$$p(s_i | c_i^k) = \frac{1}{\sqrt{2\pi}\sigma}e^{0.5(\frac{d}{\sigma})^2}$$


#### Transition probability
For the transition probability we use the distance difference $d_t$ between the great circle distance of the two stations $s_i, s_{i-1}$ and the length of the road path between the two candidate nodes $c_i^k, c_{i-1}^j$ and apply out weighting function with tuning parameter $\beta$

$$d_t = | \|s_i - s_{i-1}\|_{\text{great circle}} - \| c_i^k - c_{i-1}^j \|_{\text{route}} |$$
$$p(c_i^k \rightarrow c_{i-1}^j)=\frac{1}{\beta}e^{\frac{d_t}{\beta}}$$

### Turn restrictions
