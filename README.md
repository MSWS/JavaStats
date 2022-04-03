# JavaStats
### An automated statistic generator for [EdgeGamer's](http://edgegamers.com) GameTracker servers.
Currently hosted [here](http://egostats.msws.xyz), with the first thread [here](https://www.edgegamers.com/threads/221595/page-138#post-3351626).

## Implementation
Originally built with Heroku and Spring, JavaStats scrapes [GameTracker's](https://www.gametracker.com/) websites and saves the data. Supported storage methods are flatfile and AWS (permissions set via env variables).

## DataSnapshots
Exactly what the name implies. Important to note that modifications to the DataSnapshot class will affect all previous and future snapshot structures (due to how they are parsed/saved).