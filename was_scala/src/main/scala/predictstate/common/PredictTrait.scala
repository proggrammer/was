package predictstate.common
case class InputDeathConfirmedRecoveredData(date: String, confirmed: Int, death:Int, recovered:Int, region: String)
case class OutputDeathConfirmedRecoveredData(date: String, dumbPrediction: InfectionEstimate, smartPrediction:InfectionEstimate)
case class InfectionEstimate(alreadyInfectedAvg:Int, alreadyInfectedBest:Int, alreadyInfectedWorst:Int)

trait PredictTrait {
  def readData(path:String): List[InputDeathConfirmedRecoveredData] = {
    null
  }
  def transform(listDates: List[String], inputs: List[InputDeathConfirmedRecoveredData]):List[OutputDeathConfirmedRecoveredData]
  def write(out:List[OutputDeathConfirmedRecoveredData], outPath: String): Unit = {
  }
  def run(inpath: String="", dates: List[String]=null, outpath: String=""): Unit =  {
    write(transform(dates, readData(inpath)), outpath)
  }
}