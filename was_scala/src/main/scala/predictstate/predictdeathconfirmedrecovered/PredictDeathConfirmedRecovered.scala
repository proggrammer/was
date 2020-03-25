package predictstate.predictdeathconfirmedrecovered

import predictstate.common.{InputDeathConfirmedRecoveredData, OutputDeathConfirmedRecoveredData, PredictTrait}


object PredictDeathConfirmedRecovered {

  def r0(confirmedCases: List[Int], i: Int, avgIncubationPeriod:Int): (Int, Int, Double) = {
    (confirmedCases(i), confirmedCases(i+avgIncubationPeriod),
      (confirmedCases(i+avgIncubationPeriod)-confirmedCases(i))*1.0/confirmedCases(i))
  }

  //R0
  def reproductionNumberR0(confirmedCases: List[Int], avgIncubationPeriod: Int): List[(Int,Int,Double)]  ={
    List.range(0, confirmedCases.size-avgIncubationPeriod).map(r0(confirmedCases, _, avgIncubationPeriod))
  }
}
