import { useEffect } from "react";
import { useLocation } from "react-router-dom";

/** Resets window scroll when the route changes (SPAs do not do this automatically). */
export default function ScrollToTop() {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: "instant" });
    document.documentElement.scrollTop = 0;
    document.body.scrollTop = 0;

    const main = document.getElementById("main-content");
    if (main instanceof HTMLElement) {
      main.scrollTop = 0;
    }
  }, [pathname]);

  return null;
}
