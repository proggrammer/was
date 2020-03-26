package predictstate.predictdeathconfirmedrecovered

import predictstate.common.{InputDeathConfirmedRecoveredData, OutputDeathConfirmedRecoveredData, PredictTrait}
import spray.json.DefaultJsonProtocol

import scala.util.control.Breaks._
import scala.collection.mutable.ListBuffer

case class EveryDayPrediction(input:List[Long], r0: List[Double], minRateList: List[Long], minRate:Double, lastDateRateList: List[Long],
                              lastDateRate:Double, maxRateList: List[Long], maxRate:Double, lastDate: String, province: String)
object PredictDeathConfirmedRecovered {
  val WorldPopulation = 7800000000l;
  def r0(confirmedCases: List[Long], i: Int, avgIncubationPeriod:Int): (Long, Long, Double) = {
    (confirmedCases(i), confirmedCases(i+avgIncubationPeriod),
      (confirmedCases(i+avgIncubationPeriod)-confirmedCases(i))*1.0/confirmedCases(i))
  }

  //R0
  def reproductionNumberR0(confirmedCases: List[Long], avgIncubationPeriod: Int): List[(Long,Long,Double)]  = {
    List.range(0, confirmedCases.size-avgIncubationPeriod).map(r0(confirmedCases, _, avgIncubationPeriod))
  }

  def extrapolation(confirmedCases: List[Long], avgIncubationPeriod: Int, numberOfDays: Int, lastDate:String, province:String):  EveryDayPrediction = {
    val r0 = reproductionNumberR0(confirmedCases, avgIncubationPeriod)

    val min = r0.minBy(_._3)._3
    val max = r0.maxBy(_._3)._3
    val nowRate = (confirmedCases.last -
      confirmedCases(confirmedCases.size-1-avgIncubationPeriod)).toDouble/confirmedCases(confirmedCases.size-1-avgIncubationPeriod)
    val minR= extrapolateEach(confirmedCases, numberOfDays, min)
    val avgR = extrapolateEach(confirmedCases, numberOfDays, nowRate)
    val maxR = extrapolateEach(confirmedCases, numberOfDays, max)
    val r = EveryDayPrediction(confirmedCases, r0.map(_._3),minR, min, avgR,nowRate, maxR, max, lastDate, province)
    r
  }

  def extrapolateEach(confirmedCases:List[Long], numberOfDays:Int, rate: Double): List[Long] ={
    val listBuffer = new ListBuffer[Long]
    breakable(
      for(i <- 0 until numberOfDays)  {
        val base = if(i<5) confirmedCases(confirmedCases.size-1-4+i) else listBuffer(i-4)
        val updatedValue = base.toLong + (rate*base).round
        listBuffer.append(updatedValue)
        if(updatedValue>WorldPopulation) break()
      }
    )
    listBuffer.toList
  }
}
