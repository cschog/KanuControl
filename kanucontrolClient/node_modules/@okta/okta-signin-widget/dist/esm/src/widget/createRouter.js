import '../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Enums from '../util/Enums.js';

function createRouter(Router, widgetOptions, renderOptions, authClient, successFn, errorFn, hooks) {
  let resolve;
  let reject;
  const promise = new Promise((_resolve, _reject) => {
    resolve = _resolve;
    reject = _reject;
  });
  const routerOptions = oktaJQueryStatic.extend(true, {}, widgetOptions, renderOptions, {
    authClient: authClient,
    hooks: hooks,
    globalSuccessFn: res => {
      successFn && successFn(res); // call success function if provided

      if (res && res.status === Enums.SUCCESS) {
        resolve(res);
      }
    },
    globalErrorFn: error => {
      errorFn && errorFn(error); // call error function if provided

      reject(error);
    }
  });
  const router = new Router(routerOptions);
  router.start();
  return {
    router: router,
    routerOptions: routerOptions,
    promise: promise
  };
}

export { createRouter as default };
//# sourceMappingURL=createRouter.js.map
