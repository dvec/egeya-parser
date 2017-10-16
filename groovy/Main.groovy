final TARGET_URL = "http://maximilyahov.ru/blog/"
// final TARGET_URL = "http://zaytsev.io/blog/"

def getter = new HTMLGetter(TARGET_URL)
result = getter.perform()
println " DATE: ${result.date} \n HEADER: ${result.header} \n IMG: ${result.img} \n URL: ${result.url}"