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
import oktaUnderscore from '../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import { ConfigError } from '../util/Errors.js';
import util from '../util/OAuth2Util.js';

function buildRenderOptions(widgetOptions = {}, options = {}) {
  const authParams = oktaJQueryStatic.extend(true, {}, widgetOptions.authParams, oktaUnderscore.pick(options, util.AUTH_PARAMS));
  const {
    el: el,
    clientId: clientId,
    redirectUri: redirectUri
  } = Object.assign({}, widgetOptions, options);
  const renderOptions = Object.assign({}, {
    el: el,
    clientId: clientId,
    redirectUri: redirectUri,
    authParams: authParams
  });

  if (!renderOptions.el) {
    throw new ConfigError('"el" is required');
  }

  if (!renderOptions.clientId) {
    throw new ConfigError('"clientId" is required');
  }

  if (!renderOptions.redirectUri) {
    throw new ConfigError('"redirectUri" is required');
  }

  return renderOptions;
}

export { buildRenderOptions as default };
//# sourceMappingURL=buildRenderOptions.js.map
