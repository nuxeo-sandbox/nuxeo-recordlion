nuxeo-recordlion
===================

**WARNING**: This plugin is a Proof of Concept. 

THIS IS **WORK IN PROGRESS**

We are very, very sorry the code and way it works is poorly documented, but we are in a big hurry :-).

**IMPORTANT**: So far, passing the full do URI to some API does not work => _we rely on the dc:title_, so in this context, please make sure your titles are uniques...

**IMPORTANT**: Unit tests are almost all  because they require a runnning record lion server. See the code (SimpleFeatureCustom, mainly) to test with your recordLion server.

## Principles

Assuming you are familiar with RecordLion.

The plugin exposes a service to connect to a RecordLion (Gimmal) server. COnfiguration is done by adding configuraiton parameters:

```
nuxeo.recordlion.baseurl=THE_BASE_URL (example: https://my.recordlion.server.com)
nuxeo.recordlion.login=THE_LOGIN_FOR_A_SERVICE_ACCESS
nuxeo.recordlion.password=THE_PASSWORD_FOR_THE_SERVICE_ACCESS_LOGIN
nuxeo.recordlion.defaultRecordClassId=THE°RECORDCLASS_TO_USE_IN_SOME_API
```

Example:

```
nuxeo.recordlion.baseurl=https://my.recordlion.server.com
nuxeo.recordlion.login=connectore-mycompany@mycompany.com
nuxeo.recordlion.password=Abc*!)%345
nuxeo.recordlion.defaultRecordClassId=6
```

The plugin does not discover RecordClasses, you must know the RecordClass you will be using, and when _recordizing_ a document, if `isManuallyClassified` is `true`, you must pass a valid RecordClassId. If not passed (or `0`), the value of `nuxeo.recordlion.defaultRecordClassId` is used.

The POC was limited to creating a record for retention. So, our test recordLion server has a policy which starts the retention as soon as a record is created. Please, refer to RecordLion SDK and documentation, but basically, here is the regular process:

1. Your first _recordize_  the document. In the context of this plugin we recommend you create with `isManuallyClassified` set to `true` and a valid default RecordClassId.

    This uses `RecordLionService#recordizeDocument`
    
2. RecordLion creates the document, and checks if there is a LifeCyle or any related actions to perform
3. RecordLion then prepares _Action Items_
4. You mus regularly pull RecordLion on this document to check if what you have to do. In our example POC, we wait for the `DeclareRecord` action. **WARNING** This means we loop and call the RecordLion server in a loop. If you know the retention policy and it starts in a year, test in a year :-) This uses `RecordLionService#pullActions`. In this POC, you are suppose to _locK_  your Nuxeo document, it is in retention and should not be changed.
5. Now that you catched the `DeclareRecord`, you must perform the final creation, via `RecordLionService##declareRecordForIdentifier`.

### Note: RecordLionService#createRecord

The `RecordLionServce#createRecord` API performs the previous steps in one single call, for convenience and in the scope of this POC. This means that **the LikeCycle bound to the RecordClass on your RecordLion server _must_ DeclareRecord as soon as it is created**. A timeout of 3 minutes will apply, just in case it takes a bit more time.

The operation:

* **ID**: `RecordLion.CreateRecord`, label `"RecordLion: Create Record"`
* **Input**:
  * A `DocumentModel`
  * See above in the intro: **MAKE SURE `dc:title`  IS UNIQUE**
* **Parameters**
  * `resultVarName`: The name of the Context Variable that will receive the JSON String of the result (see below)
  * `recordClassId`:
    * The RecordClass ID to use if `isManuallyClassified` is ` true`.
    * The default class will be read in the configuration if `recordClassId` is not passed or 0.
    * As per RecordLion API, if `isManuallyClassified` is `false`, `recordClassId` is ignred (not even passed to the server)
  * `isManuallyClassified`: See `recordClassId`
  * `timeOutInSeconds`: Time to wait before giving up in the loop that fetches actions (`RecordLionService#pullActions`). Default value is 60
* The operation returns the input document unmodified and set the `resultVarName` to a JSON Object as String with the following fields:

    ```
    {
      "result": "OK", // or "KO" in case of failure,
      "uri": the URI used when saving the record
      "title": The title of the RecordLion record (same as dc:title)
      "recordidentifier": The identifier, usable with RecordLionServioce#declareRecordForIdentifier
    }
    ```

Example of use


## Using the RecordLion.CReateRecord operation


## Build

Assuming maven and node are correctly setup on your computer:

```
git clone 
mvn clean install
```

## Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep) and packaged applications for [document management](http://www.nuxeo.com/en/products/document-management), [digital asset management](http://www.nuxeo.com/en/products/dam) and [case management](http://www.nuxeo.com/en/products/case-management). Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information at <http://www.nuxeo.com/>
