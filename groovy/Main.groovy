import groovy.sql.Sql

import java.nio.file.Paths

final SQL_CONFIG_PATH = Paths.get("config/sql.xml").toAbsolutePath()
final TARGETS_PATH = Paths.get("config/targets.xml").toAbsolutePath()
final TABLE_NAME = "posts"

def sqlConfig = new XmlSlurper().parse(SQL_CONFIG_PATH.toFile())

def sql = Sql.newInstance(sqlConfig.getProperty("url").toString(), sqlConfig.getProperty("user").toString(),
        sqlConfig.getProperty("password").toString(), "org.postgresql.Driver")

sql.execute(String.format("CREATE TABLE IF NOT EXISTS %s (date CHAR(64), header CHAR(64), img CHAR(64), url CHAR(64))", TABLE_NAME))

def targets = new XmlSlurper().parse(TARGETS_PATH.toFile())
while (true) {
    for (def url : targets.children()) {
        HTMLGetter getter = new HTMLGetter(url.toString())
        getter.loadHTML()
        def result = getter.perform()
        long postCount = sql.rows(String.format("SELECT COUNT(*) FROM %s", TABLE_NAME)).first().getProperty("count")
        if (postCount >= result.date.size()) continue
        for (int i = 0; i < result.date.size(); i++) {
            sql.execute(String.format("INSERT INTO %s VALUES ('%s', '%s', '%s', '%s')",
                    TABLE_NAME, result.date[i], result.header[i], result.img[i], result.url[i]))
        }
    }
    sleep(1000 * 60 * 60)
}
sql.close()