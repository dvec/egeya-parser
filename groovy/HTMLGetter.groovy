import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class HTMLGetter {
    // TODO REWRITE (POST IS USED AS A VERSION CONFIG, WHEN IT SHOULDN'T)
    class Post {
        String header
        String date
        String img
        String url
    }

    final versions = [
            new Post(
                    header: "main/div [class='container']/article/header/h2/a",
                    date: "main/div/article/header/p/time@datetime",
                    img: "main/div/article/section/figure/a/img@src",
                    url: "main/div/article/header/h2/a@href"),
            new Post(
                    header: "div [class='common']/div [class='content']/div [id='e2-note-*']/article/h1/a/span",
                    date: "div [class='common']/div [class='content']/div [id='e2-note-*']/div [class='e2-note-tags]/span@title",
                    img: "div [class='common']/div [class='content']/div [id='e2-note-*']/article/div/div/div/div/img@src",
                    url: "div [class='common']/div [class='content']/div [id='e2-note-*']/article/h1/a@href")

    ]

    String url
    Element body
    HTMLParser parser

    HTMLGetter(String url) {
        this.url = url
        this.body = Jsoup.connect(url).get().body()
        this.parser = new HTMLParser(this.body)
    }

    private static def isExternalUrl(String url) {
        if (url.length() > 0) {
            return url.charAt(0) != '/'
        } else {
            // TODO WRITE IN LOG
            throw new RuntimeException("Empty URL")
        }
    }
    private def getHostUrl() {
        URL uri = new URL(url)
        return uri.getProtocol() + "://" + uri.getHost()
    }

    def perform() {
        for (v in versions) {
            def header = parser.run(v.header)
            def date = parser.run(v.date)
            def img = parser.run(v.img)
            def url = parser.run(v.url)

            for (int i = 0; i < img.size(); i++) {
                if (img[i] != null && !isExternalUrl(img[i])) {
                    img[i] = getHostUrl() + img[i]
                }
                if (url[i] != null && !isExternalUrl(url[i])) {
                    url[i] = getHostUrl() + url[i]
                }
            }

            if (header != [] || date != [] || img != [] || url != []) {
                return new Post(header: header, date: date, img: img, url: url)
            }
        }
        return null
    }
}