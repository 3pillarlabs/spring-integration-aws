Spring Integration AWS
======================

A Java library built on top of the Spring Integration Framework to integrate with Amazon Simple Notification Service (SNS) and Amazon Simple Queue Service (SQS). These services are part of the Amazon Web Services (AWS).


New in Release 2.0.0
--------------------

* Ability to set permissions and policies on SNS topics and SQS queues for collaboration with external systems which share these resources
* Upgrade to AWS SDK 1.8.2
* Upgrade to Spring Integration 3.0.3.RELEASE, compatible with Spring Framework 3.2.8.RELEASE
* Added demo application to main source code


Demo
----

Please [visit our demo app](http://labs.3pillarglobal.com/spring-integration-aws-demo/) to see the adapters in action.

SQS Integration Features
-------------------------

* Supports both event driven and polling messaging model 
* Works with POJO message payloads (auto-serialization)
* Handles messages pushed from a SNS topic
* Auto-acknowledge SQS messages
* Transfers Spring Integration `Message` headers to SQS message headers and vice-versa
* Auto create queues if not available

SNS Integration Features
-------------------------

- Supports both outgoing messages and incoming messages via a transparent HTTP(s) endpoint
- Support for SQS subscriptions and permissions
- Publish subscribe channel for multiple subscribers
- Signature verification of incoming messages to HTTP endpoint
- Auto create SNS topics if they do not exist

Maven Artifacts
----------------

The project artifacts are available from Maven Central. The dependency information is as follows:

    <dependency>
        <groupId>com.3pillarglobal.labs</groupId>
        <artifactId>spring-integration-aws</artifactId>
        <version>2.0.0</version>
    </dependency>

The GitHub Wiki contains information on configuring the channels and adpaters.

Reporting Issues
-----------------

Please report issues or suggestions to GitHub. Pull requests are welcome!

License
--------

Spring Integration AWS is distributed under the MIT license (http://opensource.org/licenses/MIT)

**The MIT License (MIT)**

Copyright (c) 2013 3PillarGlobal

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

# About this project

![3Pillar Global] (http://www.3pillarglobal.com/wp-content/themes/base/library/images/logo_3pg.png)

**Spring Integration AWS** is developed and maintained by [3Pillar Global](http://www.3pillarglobal.com/).

