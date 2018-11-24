# jarkeeper.com

[![Dependencies Status](https://jarkeeper.com/hashobject/jarkeeper.com/status.svg)](http://jarkeeper.com/hashobject/jarkeeper.com)
[![Circle CI](https://circleci.com/gh/hashobject/jarkeeper.com.svg?style=svg)](https://circleci.com/gh/hashobject/jarkeeper.com)
[![Chat](http://chat.gitrun.com/images/gitchat-badge.svg)](http://chat.gitrun.com/room/hashobject/jarkeeper.com/11)

## TODO

  * add support for webhooks. regenerate page after code was pushed.
  * add github auth. we can send notification to owner that deps were outdated.
  * add checks for alpha/beta versions, snapshots


## Inspirations

  * https://github.com/xsc/ancient-clj
  * https://github.com/rodnaph/clj-deps
  * https://david-dm.org
  * https://gemnasium.com/


## Ideas

  * amount of downloads last day, week, month
  * new version publish dates
  * commits activity/density
  * number of contributors - basically we want to know if project is actively maintained
  * we want to know how many projects use current one as dependency
  * look into ClojureSphere API  - https://github.com/jkk/clojuresphere
  * receive some notification when new versions are available
  * update some dedicated branch with new versions so they can be tested by continuous integration system for early feedback
  * use https://github.com/jonase/eastwood to analyze code

## Deploy

```
now
now alias jarkeeper
now alias https://jarkeeper.now.sh/ jarkeeper.com
```
## License

Copyright © 2013-2018 Hashobject Ltd (team@hashobject.com).

Distributed under the [Eclipse Public License](http://opensource.org/licenses/eclipse-1.0).
