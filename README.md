Spring Integration AWS
======================

A Java library built on top of the Spring Integration Framework to integrate with Amazon Simple Notification Service (SNS) and Amazon Simple Queue Service (SQS). These services are part of the Amazon Web Services (AWS).

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
        <version>1.1</version>
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

