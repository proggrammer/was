package predictstate

import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import org.scalatest.FunSuite
import predictstate.predictdeathconfirmedrecovered.{EveryDayPrediction, PredictDeathConfirmedRecovered}
import spray.json._
object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val everyDayPredictionFormat = jsonFormat10(EveryDayPrediction)
}

class PredictTraitTest extends FunSuite {

  test("writing PredictDeathConfirmedRecovered output") {
    val reader1 = CSVReader.open(new File("~/was/data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"))
    val reader2 = CSVReader.open(new File("~/was/data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv"))

    val r1 = reader1.all()
    val result1 = r1.tail.map(row => (row(0), row(1), row.drop(4).map(_.toLong)))

    val rr1: List[Long] = List.range(0, result1.head._3.size).map(col => result1.map(_._3(col)).sum)
    val r0Result = PredictDeathConfirmedRecovered.reproductionNumberR0(rr1, 5)
    val writer = CSVWriter.open("/Users/kapil/was/derived_data/reproduction_number.csv")
    writer.writeAll(List(r1.head.drop(4), r0Result.map(_._3.toString)))
    writer.close()
  }

  def addDays(minDate: Date, i: Int): Date = {
    val c = Calendar.getInstance()
    c.setTime(minDate);
    c.add(Calendar.DATE, i);
    c.getTime
  }

  test("min avg max") {
    val curDir:String = Paths.get(System.getProperty("user.dir")).getParent.toString
    val confirmedGlobalReader = CSVReader.open(new File(curDir+"/data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"))

    val confirmedGlobalFileContent = confirmedGlobalReader.all()

    //val allProvince = confirmedGlobalFileContent.map(x=> (x(0),x(1)))

    val minDateS = confirmedGlobalFileContent.head(4)
    val maxDateS = confirmedGlobalFileContent.head.last

    val dateFormat = new SimpleDateFormat("MM/dd/yy")
    val dateFormatOutput = new SimpleDateFormat("yy_MM_dd")
    val minDate = dateFormat.parse(minDateS)
    val maxDate = dateFormat.parse(maxDateS)

    var lastDate:Date = addDays(minDate, 6)
    var dayDiff = 6;

    while(!lastDate.after(maxDate)) {
      val lastDateS = dateFormatOutput.format(lastDate)
      val countryWithData = confirmedGlobalFileContent.tail.map(row => (row(0), row(1), row.slice(4, dayDiff+4).map(_.toLong)))

      val dataTillNow: List[Long] = List.range(0, countryWithData.head._3.size).map(col => countryWithData.map(_._3(col)).sum)

      val r = PredictDeathConfirmedRecovered.extrapolation(dataTillNow, 5, 1000, lastDateS,
        "Global");
      import MyJsonProtocol._
      val rJson = r.toJson
      val rString = rJson.prettyPrint

      import java.io._
      val filff = new File(curDir + "/derived_data/was_scala/predict_death_confirmed_recovered/global/" +
        lastDateS.toString.replace("/", "_") + ".json")
      if(!filff.getParentFile.exists())
        filff.getParentFile().mkdirs()
      val pw = new PrintWriter(filff)
      pw.write(rString)
      pw.close
      lastDate = addDays(lastDate, 1)
      dayDiff +=1
    }

    val countryWiseFileContent = confirmedGlobalFileContent.tail.groupBy(_(1)).mapValues(_.map(_.drop(4).map(_.toLong)))
    val reducedCountryWise =   countryWiseFileContent.mapValues(listOfList => listOfList.foldLeft(List.fill(listOfList.head.size)(0l))((r,next)=> (r, next).zipped.map(_ + _)))

    for(kv <- reducedCountryWise)  {
      val countryName = kv._1
      val values = kv._2
      var lastDate:Date = addDays(minDate, 6)
      var dayDiff = 6
      while(!lastDate.after(maxDate)) {
            val lastDateS = dateFormatOutput.format(lastDate)
            val dataTillNowAll: List[Long] = kv._2.take(dayDiff)
            val firstNonZeroIndex = dataTillNowAll.zipWithIndex.find(_._1!=0l).getOrElse((0,dataTillNowAll.size))._2

            val dataTillNow = dataTillNowAll.drop(firstNonZeroIndex)
            if (dataTillNow.size >=6) {
              val r = PredictDeathConfirmedRecovered.extrapolation(dataTillNow, 5, 1000, lastDateS,
                countryName)
              import MyJsonProtocol._
              val rJson = r.toJson
              val rString = rJson.prettyPrint

              import java.io._
              val filff = new File(curDir + "/derived_data/was_scala/predict_death_confirmed_recovered/" +
                countryName.toLowerCase.replace(" ", "_") + "/" +
                lastDateS.toString.replace("/", "_") + ".json")
              if (!filff.getParentFile.exists())
                filff.getParentFile().mkdirs()
              val pw = new PrintWriter(filff)
              pw.write(rString)
              pw.close
            }
            lastDate = addDays(lastDate, 1)
            dayDiff +=1
          }
    }
  }
}
