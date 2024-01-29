import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn from '../../../util/Animations.js';
import LoadingBeacon from './LoadingBeacon.js';

const NO_BEACON_CLS = 'no-beacon';
const LOADING_BEACON_CLS = 'beacon-small beacon-loading';

function isLoadingBeacon(beacon) {
  return beacon && beacon.equals(LoadingBeacon);
}

function removeBeacon(view) {
  // There are some timing issues with removing beacons (i.e. the case of
  // transitioning from loadingBeacon -> loadingBeacon)
  if (!view.currentBeacon) {
    return;
  }

  view.currentBeacon.remove();
  view.currentBeacon = null;
}

function addBeacon(view, NextBeacon, selector, options) {
  view.add(NextBeacon, {
    selector: selector,
    options: options
  });
  view.currentBeacon = view.first();
}

function typeOfTransition(currentBeacon, NextBeacon, options) {
  if (!currentBeacon && !NextBeacon) {
    return 'none';
  } // Show Loading beacon


  if (!currentBeacon && options.loading) {
    return 'load';
  } // Swap/Hide Loading beacon


  if (currentBeacon && isLoadingBeacon(currentBeacon)) {
    return NextBeacon ? 'swap' : 'unload';
  }

  if (currentBeacon && currentBeacon.equals(NextBeacon, options)) {
    return 'same';
  }

  if (!currentBeacon && NextBeacon) {
    return 'add';
  }

  if (currentBeacon && !NextBeacon) {
    return 'remove';
  }

  if (currentBeacon instanceof NextBeacon) {
    return 'fade';
  } // If none of the above
  // then we are changing the type of beacon
  // ex. from SecurityBeacon to FactorBeacon


  return 'swap';
}

class Header extends View {
  constructor(...args) {
    super(...args);
    this.currentBeacon = void 0;
  }

  preinitialize(...args) {
    this.currentBeacon = null;
    /* eslint-disable @okta/okta/no-unlocalized-text-in-templates */

    this.template = _Handlebars2.template({
      "1": function (container, depth0, helpers, partials, data) {
        var helper,
            alias1 = depth0 != null ? depth0 : container.nullContext || {},
            alias2 = container.hooks.helperMissing,
            alias3 = "function",
            alias4 = container.escapeExpression,
            lookupProperty = container.lookupProperty || function (parent, propertyName) {
          if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
            return parent[propertyName];
          }

          return undefined;
        };

        return "<h1><img src=\"" + alias4((helper = (helper = lookupProperty(helpers, "logo") || (depth0 != null ? lookupProperty(depth0, "logo") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
          "name": "logo",
          "hash": {},
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 71
            },
            "end": {
              "line": 1,
              "column": 79
            }
          }
        }) : helper)) + "\" class=\"auth-org-logo\" alt=\"" + alias4((helper = (helper = lookupProperty(helpers, "logoText") || (depth0 != null ? lookupProperty(depth0, "logoText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
          "name": "logoText",
          "hash": {},
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 108
            },
            "end": {
              "line": 1,
              "column": 120
            }
          }
        }) : helper)) + " logo\" aria-label=\"" + alias4((helper = (helper = lookupProperty(helpers, "logoText") || (depth0 != null ? lookupProperty(depth0, "logoText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
          "name": "logoText",
          "hash": {},
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 139
            },
            "end": {
              "line": 1,
              "column": 151
            }
          }
        }) : helper)) + " logo\"></h1>";
      },
      "compiler": [8, ">= 4.3.0"],
      "main": function (container, depth0, helpers, partials, data) {
        var stack1,
            lookupProperty = container.lookupProperty || function (parent, propertyName) {
          if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
            return parent[propertyName];
          }

          return undefined;
        };

        return "<div class=\"okta-sign-in-header auth-header\">" + ((stack1 = lookupProperty(helpers, "if").call(depth0 != null ? depth0 : container.nullContext || {}, depth0 != null ? lookupProperty(depth0, "logo") : depth0, {
          "name": "if",
          "hash": {},
          "fn": container.program(1, data, 0),
          "inverse": container.noop,
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 45
            },
            "end": {
              "line": 1,
              "column": 170
            }
          }
        })) != null ? stack1 : "") + "<div data-type=\"beacon-container\" class=\"beacon-container\"></div></div><div class=\"auth-content\"><div class=\"auth-content-inner\"></div></div>";
      },
      "useData": true
    });
    /* eslint-enable @okta/okta/no-unlocalized-text-in-templates */

    View.prototype.preinitialize.apply(this, args);
  } // Attach a 'no-beacon' class if the security image feature
  // is not passed in to prevent the beacon from jumping.


  initialize(options) {
    if (!options.settings.get('features.securityImage')) {
      this.$el.addClass(NO_BEACON_CLS); // To show/hide the spinner when there is no security image,
      // listen to the appState's loading/removeLoading events.

      this.listenTo(options.appState, 'loading', this.setLoadingBeacon);
      this.listenTo(options.appState, 'removeLoading', this.removeLoadingBeacon);
    }
  }
  /* eslint complexity: 0 */


  setBeacon(NextBeacon, options) {
    const selector = '[data-type="beacon-container"]';
    const container = this.$(selector);
    const transition = typeOfTransition(this.currentBeacon, NextBeacon, options);

    switch (transition) {
      case 'none':
        this.$el.addClass(NO_BEACON_CLS);
        return;

      case 'same':
        return;

      case 'add':
        this.$el.removeClass(NO_BEACON_CLS);
        addBeacon(this, NextBeacon, selector, options);
        return fn.explode(container);

      case 'remove':
        this.$el.addClass(NO_BEACON_CLS);
        return fn.implode(container).then(() => {
          removeBeacon(this);
        }).done();
      // TODO: can this be removed if Animations.implode returns standard ES6 Promise?

      case 'fade':
        // Other transitions are performed on the beacon container,
        // but this transition is on the content inside the beacon.
        // For a SecurityBeacon the username change will update the
        // AppState and trigger an transition to a new Becon
        // Since there is no url change this method is not called.
        // For a FactorBeacon a page refresh has occurred
        // so we execute the beacon's own transition method.
        if (!this.currentBeacon.fadeOut) {
          throw new Error('The current beacon is missing the "fadeOut" method');
        }

        options.animate = true;
        return this.currentBeacon.fadeOut().then(() => {
          removeBeacon(this);
          addBeacon(this, NextBeacon, selector, options);
        }).done();
      // TODO: can this be removed if fadeOut returns standard ES6 Promise?

      case 'swap':
        return fn.swapBeacons({
          $el: container,
          swap: () => {
            const isLoading = isLoadingBeacon(this.currentBeacon); // Order of these calls is important for -
            // loader --> security/factor beacon swap.

            removeBeacon(this);

            if (isLoading) {
              container.removeClass(LOADING_BEACON_CLS);
              this.$el.removeClass(NO_BEACON_CLS);
            }

            addBeacon(this, NextBeacon, selector, options);
          }
        }).done();
      // TODO: can this be removed if Animations.swapBeacons returns standard ES6 Promise?

      case 'load':
        // Show the loading beacon. Add a couple of classes
        // before triggering the add beacon code.
        container.addClass(LOADING_BEACON_CLS);
        addBeacon(this, NextBeacon, selector, options);
        return fn.explode(container);

      case 'unload':
        // Hide the loading beacon.
        return this.removeLoadingBeacon();

      default:
        throw new Error('the "' + transition + '" is not recognized');
    }
  } // Show the loading beacon when the security image feature is not enabled.


  setLoadingBeacon(isLoading) {
    if (!isLoading || isLoadingBeacon(this.currentBeacon)) {
      return;
    }

    this.setBeacon(LoadingBeacon, {
      loading: true
    });
  } // Hide the beacon on primary auth failure. On primary auth success, setBeacon does this job.


  removeLoadingBeacon() {
    const container = this.$('[data-type="beacon-container"]');
    return fn.implode(container).then(() => {
      removeBeacon(this);
      container.removeClass(LOADING_BEACON_CLS);
    }).done(); // TODO: can this be removed if Animations.implode returns standard ES6 Promise?
  }

  getTemplateData() {
    return this.settings.toJSON({
      verbose: true
    });
  }

  getContentEl() {
    return this.$('.auth-content-inner');
  }

}

export { Header as default };
//# sourceMappingURL=Header.js.map
