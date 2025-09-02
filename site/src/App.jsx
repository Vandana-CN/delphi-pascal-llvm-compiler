import { useMemo, useState } from "react";

const samples = [
  { id: "test1", title: "FOR-DO loop 1..5", pascal: `PROGRAM TEST1;
VAR i: INTEGER;
BEGIN
  FOR i := 1 TO 5 DO
  BEGIN
    writeln(i);
  END;
END.`, expectedOutput: "1\n2\n3\n4\n5\n" },
  { id: "test2", title: "WHILE-DO increments", pascal: `PROGRAM TEST2;
VAR x: INTEGER;
BEGIN
  x := 1;
  WHILE x < 5 DO
  BEGIN
    writeln(x);
    x := x + 1;
  END;
END.`, expectedOutput: "1\n2\n3\n4\n" },
  { id: "test3", title: "FOR with BREAK/CONTINUE", pascal: `PROGRAM TEST3;
VAR i: INTEGER;
BEGIN
  FOR i := 1 TO 10 DO
  BEGIN
    IF i = 3 THEN CONTINUE;
    IF i = 7 THEN BREAK;
    writeln(i);
  END;
END.`, expectedOutput: "1\n2\n4\n5\n6\n" },
  { id: "test4", title: "Procedures & Functions", pascal: `PROGRAM TEST4;
VAR a, b, result: INTEGER;
PROCEDURE greet; BEGIN writeln('Hello from the procedure!'); END;
FUNCTION add(x, y: INTEGER): INTEGER; BEGIN add := x + y; END;
BEGIN
  a := 10; b := 20;
  greet;
  result := add(a, b);
  writeln('Sum is: ', result);
END.`, expectedOutput: "Hello from the procedure!\nSum is: 30\n" },
  { id: "test5", title: "IF / ELSE", pascal: `PROGRAM TEST5;
VAR x, y: INTEGER;
BEGIN
  x := 3; y := 99;
  IF x < 5 THEN writeln(x) ELSE writeln(y);
END.`, expectedOutput: "3\n" },
  { id: "test6", title: "Integer add + print", pascal: `PROGRAM TEST6;
VAR X: INTEGER;
BEGIN
  X := 10 + 20;
  writeln(X);
END.`, expectedOutput: "30\n" },
];

// SAFE helper: builds the correct URL for dev ('/') and GitHub Pages ('/plp-compiler-demo/')
function assetUrl(path) {
  const base = import.meta.env.BASE_URL || '/';
  const b = base.endsWith('/') ? base : base + '/';
  const p = path.startsWith('/') ? path.slice(1) : path;
  return b + p;
}

export default function App() {
  const [active, setActive] = useState(samples[0].id);
  const sample = useMemo(() => samples.find(s => s.id === active), [active]);

  return (
    <div style={{ minHeight: "100vh", background: "#f7f7f7", padding: 16 }}>
      <div style={{ maxWidth: 1100, margin: "0 auto", display: "grid", gridTemplateColumns: "260px 1fr", gap: 16 }}>
        <aside>
          <div style={{ background: "#fff", border: "1px solid #e5e7eb", borderRadius: 12, padding: 12 }}>
            <h2 style={{ margin: 0, fontSize: 16, fontWeight: 600 }}>Samples</h2>
            <div style={{ marginTop: 8, display: "grid", gap: 8 }}>
              {samples.map(s => (
                <button key={s.id} onClick={() => setActive(s.id)}
                  style={{
                    textAlign: "left", border: "1px solid #e5e7eb", borderRadius: 10,
                    padding: "10px 12px", cursor: "pointer",
                    background: active === s.id ? "#111" : "#fff",
                    color: active === s.id ? "#fff" : "#111",
                  }}>
                  {s.title}
                </button>
              ))}
            </div>
          </div>
        </aside>

        <main>
          <div style={{ background: "#fff", border: "1px solid #e5e7eb", borderRadius: 12, padding: 16 }}>
            <div style={{ display: "flex", justifyContent: "space-between", gap: 8, alignItems: "center", marginBottom: 8 }}>
              <div>
                <h3 style={{ margin: 0, fontSize: 18, fontWeight: 700 }}>{sample.title}</h3>
                <div style={{ fontSize: 12, color: "#6b7280" }}>Source · Expected output · Download IR</div>
              </div>
              <a href={assetUrl(`artifacts/${sample.id}.ll`)} download
                 style={{ border: "1px solid #e5e7eb", borderRadius: 10, padding: "8px 12px", textDecoration: "none", color: "#111" }}>
                Download IR ({sample.id}.ll)
              </a>
            </div>

            <section>
              <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4 }}>Pascal</div>
              <pre style={{ background: "#f3f4f6", padding: 12, borderRadius: 10, overflowX: "auto", fontSize: 13, lineHeight: 1.35 }}>
                {sample.pascal}
              </pre>
            </section>

            <section>
              <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4 }}>Expected Output</div>
              <pre style={{ background: "#f3f4f6", padding: 12, borderRadius: 10, overflowX: "auto", fontSize: 13, lineHeight: 1.35 }}>
                {sample.expectedOutput}
              </pre>
              <div style={{ fontSize: 12, color: "#6b7280" }}>
                To run locally: <code>lli tests\{sample.id}.ll</code>
              </div>
            </section>
          </div>

          <div style={{ marginTop: 16, background: "#fff", border: "1px solid #e5e7eb", borderRadius: 12, padding: 16, fontSize: 14, color: "#374151" }}>
            <b>How this works:</b> These are your real Pascal programs and the LLVM IR your compiler generated.
            The “Download IR” link serves the <code>.ll</code> files from <code>public/artifacts</code>. No backend.
          </div>
        </main>
      </div>
    </div>
  );
}
