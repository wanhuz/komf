# Komga and Kavita metadata fetcher

## Features

- automatically pick up added series and update their metadata and thumbnail
- manually search and identify series (http endpoints or cli commands)
- match entire library or a series (http endpoints or cli commands)

In addition, you can also install [userscript](https://github.com/Snd-R/komf-userscript) that adds komf integration
directly to komga and kavita ui allowing you to launch manual or automatic metadata identification

## Build

1. `./gradlew clean shadowjar` (output is in /build/libs)

## Run

### Jar

Requires Java 17 or higher

`java -jar komf-1.0-SNAPSHOT-all.jar <path to config>`

### Docker compose

```yml
version: "3.7"
services:
  komf:
    image: sndxr/komf:latest
    container_name: komf
    ports:
      - "8085:8085"
    user: "1000:1000"
    environment: # optional env config
      - KOMF_KOMGA_BASE_URI=http://komga:8080
      - KOMF_KOMGA_USER=admin@example.org
      - KOMF_KOMGA_PASSWORD=admin
      - KOMF_KAVITA_BASE_URI=http://kavita:5000
      - KOMF_KAVITA_API_KEY=16707507-d05d-4696-b126-c3976ae14ffb
      - KOMF_LOG_LEVEL=INFO
    volumes:
      - /path/to/config:/config #path to directory with application.yml and database file
    restart: unless-stopped
```

## Example application.yml config

```yml
komga:
  baseUri: http://localhost:8080 #or env:KOMF_KOMGA_BASE_URI
  komgaUser: admin@example.org #or env:KOMF_KOMGA_USER
  komgaPassword: admin #or env:KOMF_KOMGA_PASSWORD
  eventListener:
    enabled: false # if disabled will not connect to komga and won't pick up newly added entries
    libraries: [ ]  # listen to all events if empty
  notifications:
    libraries: [ ]  # Will send notifications if any notification source is enabled. If empty will send notifications for all libraries
  metadataUpdate:
    default:
      # Update modes is the way komf will update metadata.
      # If you're using anything other than API then your existing files might be modified with embedded metadata
      updateModes: [ API ] # can use multiple options at once. available options are API, COMIC_INFO
      aggregate: false # if enabled will search and aggregate metadata from all configured providers
      bookCovers: false # update book thumbnails
      seriesCovers: false # update series thumbnails
      postProcessing:
        seriesTitle: false # update series title
        titleType: LOCALIZED # "LOCALIZED" "ROMAJI" or "NATIVE"
        alternativeSeriesTitles: false # use other title types as alternative title option
        orderBooks: false # will order books using parsed volume or chapter number
        readingDirectionValue: # override reading direction for all series. should be one of these: LEFT_TO_RIGHT, RIGHT_TO_LEFT, VERTICAL, WEBTOON
        languageValue: # set default language for series. Must use BCP 47 format e.g. "en"
kavita:
  baseUri: "http://localhost:5000" #or env:KOMF_KAVITA_BASE_URI
  apiKey: "16707507-d05d-4696-b126-c3976ae14ffb" #or env:KOMF_KAVITA_API_KEY
  eventListener:
    enabled: false # if disabled will not connect to kavita and won't pick up newly added entries
    libraries: [ ]  # listen to all events if empty
  notifications:
    libraries: [ ]  # Will send notifications if any notification source is enabled. If empty will send notifications for all libraries
  metadataUpdate:
    default:
      # Update modes is the way komf will update metadata.
      # If you're using anything other than API then your existing files might be modified with embedded metadata
      updateModes: [ API ] # can use multiple options at once. available options are API, COMIC_INFO
      aggregate: false # if enabled will search and aggregate metadata from all configured providers
      bookCovers: false #update book thumbnails
      seriesCovers: false #update series thumbnails
      postProcessing:
        seriesTitle: false #update series title
        titleType: LOCALIZED # Can be "LOCALIZED" "ROMAJI" or "NATIVE". Sets Localized Name api field and series comicinfo field. 
        alternativeSeriesTitles: false # use other title types as alternative title option
        languageValue: # set default language for series. Must use BCP 47 format e.g. "en"
discord:
  webhooks: #list of discord webhook urls. Will call these webhooks after series or books were added
  seriesCover: false # include series cover in message. Requires imgurClientId
  imgurClientId: # client id for imgur image uploads
  templatesDirectory: "./" #path to a directory with discordWebhook.vm template
database:
  file: ./database.sqlite #database file location.
metadataProviders:
  malClientId: "" #required for mal provider. See https://myanimelist.net/forum/?topicid=1973077
  defaultProviders:
    mangaUpdates:
      priority: 10
      enabled: true
    mal:
      priority: 20
      enabled: false
    nautiljon:
      priority: 30
      enabled: false
    aniList:
      priority: 40
      enabled: false
    yenPress:
      priority: 50
      enabled: false
    kodansha:
      priority: 60
      enabled: false
    viz:
      priority: 70
      enabled: false
    bookWalker:
      priority: 80
      enabled: false
server:
  port: 8085 #or env:KOMF_SERVER_PORT
logLevel: INFO #or env:KOMF_LOG_LEVEL
```

## Metadata update config for a library

You can configure a set of metadata update options that will only be used with specified library. If no options are specified for a library
then default options will be used. kavita or komga library ids are used as library identifiers

```yaml
komga_or_kavita:
  metadataUpdate:
    default:
      aggregateMetadata: false
    library:
      09PERX1TW8GEK:
        updateModes: [ API ]
        aggregateMetadata: false
        bookCovers: false
        seriesCovers: false
        postProcessing:
          seriesTitle: false
          titleType: LOCALIZED
          alternativeSeriesTitles: false
          languageValue:
      123:
        aggregateMetadata: true
        seriesCovers: true
```

## Providers config for a library

You can configure a set of metadata providers that will only be used with specified library. If no providers are specified for a library
then default providers will be used. kavita or komga library ids are used as library identifiers

```yaml
metadataProviders:
  defaultProviders:
    mangaUpdates:
      priority: 10
      enabled: true
  libraryProviders:
    09PERX1TW8GEK:
      mangaUpdates:
        priority: 10
        enabled: true
      bookWalker:
        priority: 20
        enabled: true
    123:
      aniList:
        priority: 10
        enabled: true
      mal:
        priority: 20
        enabled: true
```

## Metadata aggregation

By default, all metadata will be fetched from the first positive match in configured providers by order of priority. If
you want to enable metadata aggregation from multiple sources you need to set `aggregateMetadata` to true in the config.

If enabled, initial metadata will be taken from the first positive match in configured providers. Additional search
request will be made to all the other configured providers and metadata will be aggregated from the results. Metadata
fields will only be set from another provider if previous provider did not have any data for that particular field. For
example provider1 did not return thumbnail in that case thumbnail will be taken from provider2

You can configure which fields each provider will have in the config both for series and books. By default, all
available fields will be fetched. Example of default fields configuration

```yml
metadataProviders:
  mangaUpdates:
    priority: 10
    enabled: true
    seriesMetadata:
      status: true
      title: true
      titleSort: true
      summary: true
      publisher: true
      readingDirection: true
      ageRating: true
      language: true
      genres: true
      tags: true
      totalBookCount: true
      authors: true
      thumbnail: true
      releaseDate: true
      links: true
      books: true
      useOriginalPublisher: true # prefer original publisher and volume information if source has data about multiple providers. If false will use english or other available publisher
      #TagName: if specified and if provider has data about publisher in that language then additional tag will be added using format ({TagName}: publisherName)
      #e.g. originalPublisherTagName: "Original Publisher" will add tag "Original Publisher: Shueisha"
      originalPublisherTagName:
      englishPublisherTagName:
      frenchPublisherTagName:
    bookMetadata:
      title: true
      summary: true
      number: true
      numberSort: true
      releaseDate: true
      authors: true
      tags: true
      isbn: true
      links: true
      thumbnail: true
```

If you want to disable particular field you just need to set the field value to false

```yml
metadataProviders:
  mangaUpdates:
    priority: 10
    enabled: true
    seriesMetadata:
      thumbnail: false
```

## Discord notifications

if any webhook urls are specified then after new book is added a call to webhooks will be triggered. You can change
message format by providing your own template file called `discordWebhook.vm` and specifying directory path to this
template in `templatesDirectory` under discord configuration. For docker deployments `discordWebhook.vm` should be
placed in mounted `/config` directory without specifying `templatesDirectory`

Templates are written using Apache Velocity ([link to docs](https://velocity.apache.org/engine/2.3/user-guide.html)).
Example of a template:

```velocity
**$series.name**

#if ($series.summary != "")
    $series.summary

#end
#if($books.size() == 1)
***new book was added to library $library.name:***
#else
***new books were added to library $library.name:***
#end
#foreach ($book in $books)
**$book.name**
#end
```

Variables available in template: `library.(name)`, `series.(id, name, summary)`, `books.(id, name)`(list of book
objects)

## Command line options

You can run komf as a daemon server or as a cli tool for one-off operation

`java -jar komf.jar [OPTIONS]`

### options:

`--config-dir` - config directory that will be used for all external files including config file. Config file must be
named `application.yml`. This option overrides all other config path options

`--config-file` - path to config file

`--verbose` - flag to enable debug messages

`--media-server` - media server on which to execute subcommands. Available values are `komga` or `kavita`. Defaults to komga if not
provided. Ignored in server mode

### Commands

subcommands will launch komf without starting server or event listener and will perform specified operation. Example of
a command:

`java -jar komf.jar --config-file=./application.yml series update 09PQAG2PDNW4V`

### series search

`komf series search NAME` - searches series in komga by specified name

### series update

`komf series update ID` - launches metadata auto identification for provided series id

### series identify

`komf series identify ID` - manual identification that allows you to choose from the list of metadata provider search
results

### series reset

`komf series reset ID` - resets all metadata for provided series id

### library update

`komf library update ID` - launches metadata auto identification for provided library id

### library reset

`komf library reset ID` - resets all metadata for provided library id

## Http endpoints

use komga or kavita in place of {media-server}

`GET /{media-server}/providers` - list of enabled metadata providers. Optional `libraryId` parameter can be used for library providers

`GET /{media-server}/search?name=...` - search results from enabled metadata providers. Optional `libraryId` parameter can be used for
library providers

`POST /{media-server}/identify` - set series metadata from specified provider

```json
{
  "libraryId": "09TDSWK3Q0XRA",
  "seriesId": "07XF6HKAWHHV4",
  "provider": "MANGA_UPDATES",
  "providerSeriesId": "1"
}
```

`POST /{media-server}/match/library/{libraryId}/series/{seriesId}`try to match series

`POST /{media-server}/match/library/{libraryId}` try to match series of a library

`POST /{media-server}/reset/library/{libraryId}/series/{seriesId}` reset all metadata of a series

`POST /{media-server}/reset/library/{libraryId}` reset metadata of all series in a library
