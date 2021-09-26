package apps.dcoder.easysftp.filemanager

import apps.dcoder.easysftp.model.FileInfo
import java.util.Comparator
import kotlin.math.min

class AlphaNumericComparator : Comparator<FileInfo> {

    private fun countConsecutiveDigits(begIndex: Int, str: String): Int {
        var cnt = begIndex
        do { cnt++ } while (cnt < str.length && str[cnt].isDigit())

        return cnt
    }

    override fun compare(o1: FileInfo, o2: FileInfo): Int {
        if(o1 == o2) {
            return 0
        } else if(o1.isDirectory && o2.isFile) {
            return -1
        } else if (o1.isFile && o2.isDirectory) {
            return 1
        }

        val o1NameLowerCase = o1.name.toLowerCase()
        val o2NameLowerCase = o2.name.toLowerCase()
        val minWordLength = min(o1NameLowerCase.length, o2NameLowerCase.length)
        val o1TrimmedName = o1NameLowerCase.substring(0, minWordLength)
        val o2TrimmedName = o2NameLowerCase.substring(0, minWordLength)

        var skipCnt = 0
        for(i in 0 until minWordLength) {
            val o1Char = o1TrimmedName[i]
            val o2Char = o2TrimmedName[i]

            if(i >= skipCnt && o1Char.isDigit() && o2Char.isDigit()) {
                val j = countConsecutiveDigits(i, o1TrimmedName)
                val k = countConsecutiveDigits(i, o2TrimmedName)

                if(j < k) {
                    return -1
                } else if(j > k) {
                    return 1
                }

                skipCnt = k - 1
            }

            val compValue = o1Char - o2Char
            if(compValue != 0) {
                return compValue
            }
        }

        return o1NameLowerCase.length - o2NameLowerCase.length
    }
}