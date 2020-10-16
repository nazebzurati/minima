package net.fionix.minima.util

import net.fionix.minima.model.EntityCourse
import net.fionix.minima.model.EntityFaculty
import net.fionix.minima.model.EntityTimetable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*
import kotlin.math.min

class ScraperICress {

    companion object {

        private const val facultyScrapableLink: String = "http://icress.uitm.edu.my/jadual/jadual/jadual.asp"
        private const val timetableScrapableLinkPrefix: String = "http://icress.uitm.edu.my/jadual/"
        private const val timeoutInMillis: Int = 500

        fun retrieveFacultyList(): ArrayList<EntityFaculty> {

            // retrieve page
            var doc: Document?
            try {
                doc = Jsoup.connect(facultyScrapableLink).timeout(timeoutInMillis).get()
            } catch (e: IOException) {
                return arrayListOf<EntityFaculty>()
            }

            // parse faculty list
            val arrayList = arrayListOf<EntityFaculty>()
            if (doc != null) {
                val list = doc.select("option")
                for (item in list) {
                    val temp: String = item.html().toString().replace(" \\[".toRegex(), ", ").replace(" &amp; ".toRegex(), " ").replace("\\(".toRegex(), "").replace("\\)".toRegex(), "").toUpperCase(Locale.getDefault())
                    arrayList.add(EntityFaculty(0, temp.substring(0, min(temp.length, 2)), temp.substring(3, temp.length)))
                }
            }

            // return
            return arrayList
        }


        fun retrieveTimetable(facultyList: ArrayList<EntityFaculty>, courseCode: String, courseGroup: String): ArrayList<EntityTimetable> {

            // retrieve page
            val arrayList = arrayListOf<EntityTimetable>()
            for (faculty in facultyList) {

                // retrieve page
                var doc: Document?
                try {
                    doc = Jsoup.connect(timetableScrapableLinkPrefix + faculty.facultyCode + "/" + courseCode + ".html").timeout(timeoutInMillis).get()
                } catch (e: IOException) {
                    continue
                }

                // add and save data
                for (table in doc.select("table")) {
                    for (row in table.select("tr")) {
                        val tds = row.select("td")
                        if (tds[0].text().toLowerCase(Locale.getDefault()).contains(courseGroup.toLowerCase(Locale.getDefault()))) {
                            val course: EntityCourse = EntityCourse(0, faculty, courseCode, courseGroup)
                            arrayList.add(EntityTimetable(0, course, tds[1].text(), tds[2].text(), tds[3].text(), tds[6].text()))
                        }
                    }
                }
            }

            // return
            return arrayList
        }
    }
}