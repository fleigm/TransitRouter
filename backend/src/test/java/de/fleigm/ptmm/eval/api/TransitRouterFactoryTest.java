package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Parameters;
import de.fleigm.ptmm.routing.DefaultTransitRouter;
import de.fleigm.ptmm.routing.GraphHopperTransitRouter;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class TransitRouterFactoryTest {

  @Inject
  TransitRouterFactory factory;

  @Test
  void can_switch_between_GHMM_and_TransitRouter() {
    Parameters parameters = Parameters.defaultParameters();

    assertEquals(DefaultTransitRouter.class, factory.create(parameters).getClass());

    parameters.setUseGraphHopperMapMatching(true);
    assertEquals(GraphHopperTransitRouter.class, factory.create(parameters).getClass());
  }


}