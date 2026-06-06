package com.example.aviationweather.data

data class AirportInfo(
    val icao: String,
    val name: String,
    val city: String,
    val country: String,
    val runways: List<String> = emptyList()
)

object AirportsData {
    val list = listOf(
        AirportInfo("KJFK", "John F. Kennedy International", "New York", "USA", listOf("04L/22R", "04R/22L", "13L/31R", "13R/31L")),
        AirportInfo("KLAX", "Los Angeles International", "Los Angeles", "USA", listOf("06L/24R", "06R/24L", "07L/25R", "07R/25L")),
        AirportInfo("KORD", "O'Hare International", "Chicago", "USA", listOf("09L/27R", "09R/27L", "10C/28C", "10L/28R", "10R/28L", "04L/22R", "04R/22L")),
        AirportInfo("KSFO", "San Francisco International", "San Francisco", "USA", listOf("01L/19R", "01R/19L", "10L/28R", "10R/28L")),
        AirportInfo("KDFW", "Dallas/Fort Worth International", "Dallas", "USA", listOf("13L/31R", "13R/31L", "17L/35R", "17C/35C", "17R/35L", "18L/36R", "18R/36L")),
        AirportInfo("KDEN", "Denver International", "Denver", "USA", listOf("07/25", "08/26", "16L/34R", "16R/34L", "17L/35R", "17R/35L")),
        AirportInfo("KATL", "Hartsfield-Jackson Atlanta", "Atlanta", "USA", listOf("08L/26R", "08R/26L", "09L/27R", "09R/27L", "10/28")),
        AirportInfo("KMIA", "Miami International", "Miami", "USA", listOf("08L/26R", "08R/26L", "09/27", "12/30")),
        AirportInfo("KSEA", "Seattle-Tacoma International", "Seattle", "USA", listOf("16L/34R", "16C/34C", "16R/34L")),
        AirportInfo("KBOS", "Boston Logan International", "Boston", "USA", listOf("04L/22R", "04R/22L", "09/27", "14/32", "15R/33L")),
        AirportInfo("KMCO", "Orlando International", "Orlando", "USA", listOf("17L/35R", "17R/35L", "18L/36R", "18R/36L")),
        AirportInfo("KPHX", "Phoenix Sky Harbor", "Phoenix", "USA", listOf("07L/25R", "07R/25L", "08/26")),
        AirportInfo("KSAN", "San Diego International", "San Diego", "USA", listOf("09/27")),
        AirportInfo("KHNL", "Daniel K. Inouye International", "Honolulu", "USA", listOf("04L/22R", "04R/22L", "08L/26R", "08R/26L")),
        AirportInfo("PANC", "Ted Stevens Anchorage", "Anchorage", "USA", listOf("07L/25R", "07R/25L", "15/33")),
        
        AirportInfo("EGLL", "London Heathrow", "London", "UK", listOf("09L/27R", "09R/27L")),
        AirportInfo("LFPG", "Paris Charles de Gaulle", "Paris", "France", listOf("08L/26R", "08R/26L", "09L/27R", "09R/27L")),
        AirportInfo("EDDF", "Frankfurt Airport", "Frankfurt", "Germany", listOf("07L/25R", "07C/25C", "07R/25L", "18")),
        AirportInfo("EHAM", "Amsterdam Schiphol", "Amsterdam", "Netherlands", listOf("04/22", "06/24", "09/27", "18C/36C", "18L/36R", "18R/36L")),
        AirportInfo("LEMD", "Madrid-Barajas", "Madrid", "Spain", listOf("14L/32R", "14R/32L", "18L/36R", "18R/36L")),
        AirportInfo("LIRF", "Rome Fiumicino", "Rome", "Italy", listOf("07/25", "16L/34R", "16R/34L")),
        AirportInfo("LSZH", "Zurich Airport", "Zurich", "Switzerland", listOf("10/28", "14/32", "16/34")),
        AirportInfo("LOWW", "Vienna International", "Vienna", "Austria", listOf("11/29", "16/34")),
        AirportInfo("LEBL", "Barcelona-El Prat", "Barcelona", "Spain", listOf("02/20", "06/24", "07L/25R", "07R/25L")),
        
        AirportInfo("CYYZ", "Toronto Pearson", "Toronto", "Canada", listOf("05/23", "06L/24R", "06R/24L", "15L/33R", "15R/33L")),
        AirportInfo("CYVR", "Vancouver International", "Vancouver", "Canada", listOf("08L/26R", "08R/26L", "13/31")),
        AirportInfo("CYEG", "Edmonton International", "Edmonton", "Canada", listOf("02/20", "12/30")),
        AirportInfo("CYUL", "Montreal-Trudeau", "Montreal", "Canada", listOf("06L/24R", "06R/24L", "10/28")),
        
        AirportInfo("SBGR", "Guarulhos International", "São Paulo", "Brazil", listOf("10L/28R", "10R/28L")),
        AirportInfo("SAEZ", "Ezeiza International", "Buenos Aires", "Argentina", listOf("11/29", "17/35")),
        AirportInfo("SCEL", "Arturo Merino Benítez", "Santiago", "Chile", listOf("17L/35R", "17R/35L")),
        
        AirportInfo("OMDB", "Dubai International", "Dubai", "UAE", listOf("12L/30R", "12R/30L")),
        AirportInfo("OTHH", "Hamad International", "Doha", "Qatar", listOf("16L/34R", "16R/34L")),
        AirportInfo("OBBI", "Bahrain International", "Manama", "Bahrain", listOf("12L/30R", "12R/30L")),
        
        AirportInfo("RJTT", "Haneda Airport", "Tokyo", "Japan", listOf("04/22", "16L/34R", "16R/34L", "23/05")),
        AirportInfo("WSSS", "Singapore Changi", "Singapore", "Singapore", listOf("02L/20R", "02C/20C", "02R/20L")),
        AirportInfo("VHHH", "Hong Kong International", "Hong Kong", "China", listOf("07L/25R", "07C/25C", "07R/25L")),
        AirportInfo("VIDP", "Indira Gandhi International", "Delhi", "India", listOf("09/27", "10/28", "11L/29R", "11R/29L")),
        AirportInfo("VABB", "Chhatrapati Shivaji Maharaj", "Mumbai", "India", listOf("09/27", "14/32")),
        AirportInfo("VOBL", "Kempegowda International", "Bengaluru", "India", listOf("09L/27R", "09R/27L")),
        AirportInfo("RKSI", "Incheon International", "Seoul", "South Korea", listOf("15L/33R", "15R/33L", "16L/34R", "16R/34L")),
        AirportInfo("RCTP", "Taoyuan International", "Taipei", "Taiwan", listOf("05L/23R", "05R/23L")),
        AirportInfo("VTBS", "Suvarnabhumi Airport", "Bangkok", "Thailand", listOf("01L/19R", "01R/19L", "02L/20R")),
        AirportInfo("RPLL", "Ninoy Aquino International", "Manila", "Philippines", listOf("06/24", "13/31")),
        AirportInfo("WIII", "Soekarno-Hatta", "Jakarta", "Indonesia", listOf("07L/25R", "07R/25L")),
        AirportInfo("WMKK", "Kuala Lumpur International", "Kuala Lumpur", "Malaysia", listOf("14L/32R", "14R/32L", "15/33")),
        AirportInfo("VVTS", "Tan Son Nhat International", "Ho Chi Minh City", "Vietnam", listOf("07L/25R", "07R/25L")),
        
        AirportInfo("VNKT", "Tribhuvan International", "Kathmandu", "Nepal", listOf("02/20")),
        AirportInfo("VNLK", "Tenzing-Hillary (Lukla)", "Lukla", "Nepal", listOf("06/24")),
        
        AirportInfo("FAOR", "O.R. Tambo International", "Johannesburg", "South Africa", listOf("03L/21R", "03R/21L")),
        AirportInfo("FACT", "Cape Town International", "Cape Town", "South Africa", listOf("01/19", "16/34")),
        AirportInfo("HECA", "Cairo International", "Cairo", "Egypt", listOf("05L/23R", "05C/23C", "05R/23L")),
        AirportInfo("HKJK", "Jomo Kenyatta International", "Nairobi", "Kenya", listOf("06/24")),
        
        AirportInfo("YSSY", "Sydney Kingsford Smith", "Sydney", "Australia", listOf("07/25", "16L/34R", "16R/34L")),
        AirportInfo("YMML", "Melbourne Airport", "Melbourne", "Australia", listOf("09/27", "16/34")),
        AirportInfo("YBBN", "Brisbane Airport", "Brisbane", "Australia", listOf("01L/19R", "01R/19L")),
        AirportInfo("YPPH", "Perth Airport", "Perth", "Australia", listOf("03/21", "06/24")),
        AirportInfo("NZAA", "Auckland Airport", "Auckland", "New Zealand", listOf("05R/23L", "05L/23R")),
        AirportInfo("NZCH", "Christchurch International", "Christchurch", "New Zealand", listOf("02/20", "11/29"))
    )
}
