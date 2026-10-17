(function() {
    'use strict';
    try {
        // 1. Hardware concurrency and Device Memory spoofing
        Object.defineProperty(navigator, 'hardwareConcurrency', {
            get: function() { return 4; },
            enumerable: true,
            configurable: true
        });

        Object.defineProperty(navigator, 'deviceMemory', {
            get: function() { return 8; },
            enumerable: true,
            configurable: true
        });

        // 2. Languages normalization
        Object.defineProperty(navigator, 'languages', {
            get: function() { return ['en-US', 'en']; },
            enumerable: true,
            configurable: true
        });

        Object.defineProperty(navigator, 'language', {
            get: function() { return 'en-US'; },
            enumerable: true,
            configurable: true
        });

        // 3. Plugins & MimeTypes emptying
        Object.defineProperty(navigator, 'plugins', {
            get: function() { return []; },
            enumerable: true,
            configurable: true
        });

        Object.defineProperty(navigator, 'mimeTypes', {
            get: function() { return []; },
            enumerable: true,
            configurable: true
        });

        // 4. Disable Battery Status API
        if ('getBattery' in navigator) {
            try {
                Object.defineProperty(navigator, 'getBattery', {
                    get: function() { return undefined; },
                    configurable: true
                });
            } catch(e) {}
        }

        // 5. Rounded screen dimensions
        var roundToHundred = function(val) {
            return Math.max(100, Math.round(val / 100) * 100);
        };
        try {
            var origW = window.screen.width;
            var origH = window.screen.height;
            Object.defineProperty(window.screen, 'width', { get: function() { return roundToHundred(origW); } });
            Object.defineProperty(window.screen, 'availWidth', { get: function() { return roundToHundred(origW); } });
            Object.defineProperty(window.screen, 'height', { get: function() { return roundToHundred(origH); } });
            Object.defineProperty(window.screen, 'availHeight', { get: function() { return roundToHundred(origH); } });
        } catch(e) {}

        // 6. WebGL Vendor & Renderer spoofing
        var hookWebGL = function(target) {
            if (!target || !target.prototype) return;
            var origGetParam = target.prototype.getParameter;
            target.prototype.getParameter = function(param) {
                // UNMASKED_VENDOR_WEBGL = 0x9245
                if (param === 0x9245 || param === 37445) {
                    return "Google Inc. (TrekScape Engine)";
                }
                // UNMASKED_RENDERER_WEBGL = 0x9246
                if (param === 0x9246 || param === 37446) {
                    return "TrekScape Generic GPU";
                }
                return origGetParam.apply(this, arguments);
            };
        };
        hookWebGL(window.WebGLRenderingContext);
        hookWebGL(window.WebGL2RenderingContext);

        // 7. Subtle Canvas Noise
        if (window.CanvasRenderingContext2D) {
            var origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
            CanvasRenderingContext2D.prototype.getImageData = function(sx, sy, sw, sh) {
                var imgData = origGetImageData.apply(this, arguments);
                var data = imgData.data;
                // Perturb 1 in 100 pixels by +/- 1
                for (var i = 0; i < data.length; i += 400) {
                    data[i] = (data[i] + 1) % 256;
                }
                return imgData;
            };

            var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
            HTMLCanvasElement.prototype.toDataURL = function() {
                var ctx = this.getContext('2d');
                if (ctx && this.width > 0 && this.height > 0) {
                    try {
                        var pixel = ctx.getImageData(0, 0, 1, 1);
                        pixel.data[0] = (pixel.data[0] + 1) % 256;
                        ctx.putImageData(pixel, 0, 0);
                    } catch(e) {}
                }
                return origToDataURL.apply(this, arguments);
            };
        }

        // 8. Subtle Audio Noise
        if (window.AudioBuffer) {
            var origGetChannelData = AudioBuffer.prototype.getChannelData;
            AudioBuffer.prototype.getChannelData = function(ch) {
                var floatData = origGetChannelData.apply(this, arguments);
                for (var j = 0; j < floatData.length; j += 200) {
                    floatData[j] += 0.0000001;
                }
                return floatData;
            };
        }
    } catch(err) {
        // Silent fail for non-compatible pages
    }
})();
