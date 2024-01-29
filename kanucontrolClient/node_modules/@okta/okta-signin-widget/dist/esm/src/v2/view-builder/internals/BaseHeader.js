import { View } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn from '../../../util/Animations.js';
import Enums from '../../../util/Enums.js';

var BaseHeader = View.extend({
  HeaderBeacon: null,
  initialize: function () {
    if (this.HeaderBeacon) {
      this.add(this.HeaderBeacon);
    }
  },
  postRender: function () {
    const mainContentContainer = oktaJQueryStatic(`#${Enums.WIDGET_CONTAINER_ID}`);

    if (this.HeaderBeacon) {
      mainContentContainer.removeClass('no-beacon'); // animate beacon

      const beaconContainer = this.$el.find('[data-type="beacon-container"]');
      fn.explode(beaconContainer);
    } else {
      mainContentContainer.addClass('no-beacon');
    }
  }
});

export { BaseHeader as default };
//# sourceMappingURL=BaseHeader.js.map
