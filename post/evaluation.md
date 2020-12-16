# Evaluation metrics

To evaluate the quality of our generated shapes we use three different metrics where we compare a generated path P with the corresponding path Q of the ground truth.

## Average Frèchet Distance $\delta_{a_F}$
We split the paths $P$ and $Q$ into an equal number of segments such that the segments in $P$ have a length of 1m. Then $\delta_{a_F}$ is the average of the Frèchet Distance between the segments in $P$ and $Q$.


## Percentage of unmatched hop segments $A_N$
A *hop segment* is the segment of a Path $P$ between two station / hops.
A *hop segment* is mismatched its Frèchet Distance is $\geq$ 20m.
$$A_N = \frac{\text{\#unmatched hop segments}}{\text{\#hop segments}}$$ 

## Percentage of length of unmatched hop segments $A_L$
$$A_L = \frac{\text{length of unmatched segments}}{\text{length ground truth}}$$
