import org.jsoup.nodes.Element

class HTMLParser {
    Element body

    HTMLParser(Element body) {
        this.body = body
    }

    private static boolean isEquals(String s1, String s2) {
        if (s1.length() > s2.length()) return false
        while (s1.contains("*")) {
            int index = s1.indexOf("*")
            int indexFromEnd =  s1.length() - index

            String s1rev = s1.reverse()[0..<indexFromEnd - 1]
            String s2rev = s2.reverse()[0..<indexFromEnd - 1]

            s1 = s1[0..<index - 1]
            s2 = s2[0..<index - 1]

            if (s1 != s2 || s1rev != s2rev) return false
        }
        return s1 == s2
    }

    private static boolean isArgumentEquals(Element e, Map<String, String> arg) {
        for (a in arg.keySet()) {
            if (!isEquals(arg[a], e.attr(a))) return false
        }
        return true
    }

    private static boolean isSuitable(Element e, String name, Map<String, String> arg) {
        return isEquals(name, e.tagName()) && isArgumentEquals(e, arg)
    }

    private static Tuple performBraces(String message) {
        if (message.contains('[') && message.contains(']')) {
            HashMap arg = [ : ]
            String arguments = message[message.indexOf('[') + 1..message.indexOf(']') - 1]
            for (String s: arguments.split(" ")) {
                String[] parts = s.split("=")
                arg[parts[0]] = parts[1].replace("'", "")
            }
            return new Tuple(message.replace("[${arguments}]", "").trim(), arg)
        } else {
            return new Tuple(message, [ : ])
        }
    }

    private static Tuple performSlice(String message) {
        if (message.contains('[') && message.contains(']')) {
            String slice = message[message.indexOf('[') + 1..message.indexOf(']') - 1]
            String[] ft = slice.split(":")
            int from, to
            if (ft.length == 1) {
                from = Integer.valueOf(ft[0])
                to = from + 1
            } else {
                if (ft.length != 2) {
                    throw new RuntimeException("Bad slice: " + slice)
                } else {
                    from = Integer.valueOf(ft[0])
                    to = Integer.valueOf(ft[1])
                }
            }
            return new Tuple(message[0..message.indexOf('[') - 1].trim(), from, to)
        }

        return new Tuple(message, 0, -1)
    }

    private static Tuple performParam(String message) {
        if (message.count('@') == 1) {
            String[] parts = message.split('@')
            return new Tuple(parts[0], parts[1])
        } else {
            return new Tuple(message, null)
        }
    }

    private def perform(List<String> p, Element e) {
        def (String message, Map<String, String> arg) = performBraces(p[0])
        def (String name, String param) = performParam(message)
        def (String nameTmp, from, to) = performSlice(name)
        if (to == -1) to = e.children().size()
        name = nameTmp

        ArrayList<String> toReturn = new ArrayList<>()
        for (c in e.children()) {
            if (from++ >= to) break
            if (!isSuitable(c, name, arg)) continue
            if (p.size() > 1) {
                toReturn += perform(p[1..-1], c)
            } else {
                if (param != null) {
                    toReturn << c.attr(param).trim()
                } else {
                    toReturn << c.text().trim()
                }
            }
        }

        if (toReturn.size() > 1) {
            return toReturn
        } else if (toReturn.size() == 1) {
            return toReturn[0]
        }
        return null
    }

    ArrayList run(String command) {
        return (ArrayList) perform(command.split("/").toList(), body) ?: []
    }
}
