package predictstate

import java.io.File

import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import org.scalatest.FunSuite
import predictstate.predictdeathconfirmedrecovered.PredictDeathConfirmedRecovered

class PredictTraitTest extends FunSuite {
  test("writing PredictDeathConfirmedRecovered output") {
    val reader1 = CSVReader.open(new File("/Users/kapil/was/data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"))
    val reader2 = CSVReader.open(new File("/Users/kapil/was/data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv"))

    val r1 = reader1.all()
    val result1 = r1.tail.map(row => (row(0), row(1), row.drop(4).map(_.toInt)))

    val rr1: List[Int] = List.range(0, result1.head._3.size).map(col => result1.map(_._3(col)).sum)

    //val result2 = reader2.all().map(row => (row(0), row(1)))
    //result1
    val r0Result = PredictDeathConfirmedRecovered.reproductionNumberR0(rr1, 5);
    //val toWrite = r0Result.map(e => (e._2, e._1._3))
    val writer = CSVWriter.open("/Users/kapil/was/derived_data/reproduction_number.csv")
    writer.writeAll(List(r1.head.drop(4), r0Result.map(_._3.toString)))
    writer.close()
  }
}
