(function () {
  var EN_PREFIX = "en";
  var INDEX = "index.html";
  var SECTIONS = [
    "fp",
    "typeclass",
    "algorithms",
    "data-structures",
    "cats-effect",
    "libs",
    "practice",
    "translations",
    "extra"
  ];

  function segments(path) {
    var rel = (path || "").replace(/^\/+/, "").replace(/\/+$/, "");
    if (rel === "") return [];
    return rel.split("/");
  }

  function dirDepth(segs) {
    return Math.max(0, segs.length - 1);
  }

  function relFrom(depth, absTarget) {
    var up = new Array(depth + 1).join("../");
    var down = absTarget === "/" ? "" : absTarget.slice(1);
    return up + down;
  }

  function safeEnglish(segs) {
    if (segs.length === 0) return "/" + EN_PREFIX + "/" + INDEX;
    if (segs.length === 1 && segs[0] === INDEX) return "/" + EN_PREFIX + "/" + INDEX;
    if (segs.length === 2 && segs[1] === INDEX &&
        SECTIONS.indexOf(segs[0]) !== -1) {
      return "/" + EN_PREFIX + "/" + segs[0] + "/" + INDEX;
    }
    return "/" + EN_PREFIX + "/" + INDEX;
  }

  function safeRussian(segs) {
    if (segs.length === 0) return "/" + INDEX;
    if (segs.length === 1 && segs[0] === INDEX) return "/" + INDEX;
    if (segs.length === 2 && segs[1] === INDEX &&
        SECTIONS.indexOf(segs[0]) !== -1) {
      return "/" + segs[0] + "/" + INDEX;
    }
    return "/" + INDEX;
  }

  function counterpart(path) {
    var segs = segments(path);
    var absTarget = (segs.length && segs[0] === EN_PREFIX)
      ? safeRussian(segs.slice(1))
      : safeEnglish(segs);
    return relFrom(dirDepth(segs), absTarget);
  }

  document.addEventListener("DOMContentLoaded", function () {
    var links = document.querySelectorAll(".language-switch");
    if (!links.length) return;
    var href = counterpart(window.location.pathname);
    for (var i = 0; i < links.length; i++) {
      links[i].setAttribute("href", href);
    }
  });
})();