package predictstate

import org.scalatest.FunSuite
import predictstate.predictdeathconfirmedrecovered.PredictDeathConfirmedRecovered

class PredictTraitTest extends FunSuite {
  test("writing PredictDeathConfirmedRecovered output") {
    PredictDeathConfirmedRecovered.run();
  }
}
