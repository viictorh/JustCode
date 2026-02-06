import { validEmails } from "./validEmails.js";
import { invalidEmails } from "./invalidEmails.js";

/* ===============================
 * REGEXES EM TESTE
 * =============================== */

const regexes = [
  {
    name: "📌 Current-GmapUI",
    regex: /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  },
  {
    name: "📌 Current-USER-MGMT",
    regex: /^([\\p{L}a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+)@((\[[\d]{1,3}\.[\d]{1,3}\.[\d]{1,3}\.[\d]{1,3}\])|(([\p{L}a-zA-Z\-0-9]+\.)+[\p{L}a-zA-Z]{2,}))$/
  },
  {
    name: "Muito simples (qualquer @ e .)",
    regex: /^[^@\s]+@[^@\s]+\.[^@\s]+$/u
  },
  {
    name: "Simples com TLD mínimo",
    regex: /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/u
  },
  {
    name: "Simples sem ponto inicial/final",
    regex: /^[^\s@.][^\s@]*@[^\s@.][^\s@]*\.[^\s@]{2,}$/u
  },
  {
    name: "Intermediária (bloqueia hífen inválido)",
    regex: /^[^\s@.\-][^\s@]*@[^\s@.\-][^\s@\-]*\.[^\s@]{2,}$/u
  },
  {
    name: "Unicode intermediária (recomendada)",
    regex: /^[^\s@.\-][^\s@]*@[^\s@.\-][^\s@]*\.[^\s@]{2,}$/u
  },
  {
    name: "Unicode com subdomínios obrigatórios",
    regex: /^[^\s@]+@([^\s@]+\.)+[^\s@]{2,}$/u
  },
  {
    name: "Unicode com TLD alfabético",
    regex: /^[^\s@]+@[^\s@]+\.[\p{L}]{2,}$/u
  },
  {
    name: "Unicode estrita (domínio bem formado)",
    regex: /^[^\s@"]+(?:\.[^\s@"]+)*@[^\s@.\-][^\s@\-]*(?:\.[^\s@\-]+)+$/u
  },
  {
    name: "Unicode com quoted local-part",
    regex: /^(?:[^\s@"]+(?:\.[^\s@"]+)*|"(?:[^"\\]|\\.)*")@[^\s@]+(?:\.[^\s@]+)+$/u
  },
  {
    name: "RFC 6531-ish (mais completa)",
    regex: /^(?:[^\s@"]+(?:\.[^\s@"]+)*|"(?:[^"\\]|\\.)*")@(?:(?:[\p{L}\p{N}-]+\.)+[\p{L}]{2,}|localhost)$/u
  },
  {
    name: "RFC 6531-ish sem quoted",
    regex: /^[^\s@"]+(?:\.[^\s@"]+)*@(?:(?:[\p{L}\p{N}-]+\.)+[\p{L}]{2,}|localhost)$/u
  },
  {
    name: "Unicode permissiva (boa para input)",
    regex: /^[^\s@]+@[^\s@]+$/u
  },
  {
    name: "Corporate-safe (sem símbolos estranhos)",
    regex: /^[\p{L}\p{N}._%+-]+@[\p{L}\p{N}-]+(?:\.[\p{L}\p{N}-]+)+$/u
  },
  {
    name: "Strict ASCII only",
    regex: /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\.[A-Za-z]{2,})+$/u
  },
  {
    name: "ASCII + subdomínios profundos",
    regex: /^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\.)+[A-Za-z]{2,}$/u
  },
  {
    name: "RFC 6531-ish (dot-safe) - nova",
    regex: /^(?:[\p{L}\p{N}\p{M}!#$%&'*+/=?^_`{|}~-]+(?:\.[\p{L}\p{N}\p{M}!#$%&'*+/=?^_`{|}~-]+)*|"(?:[^"\\]|\\.)*")@(?:(?:[\p{L}\p{N}\p{M}-]+\.)+(?:[\p{L}\p{N}\p{M}]{2,}|xn--[a-z0-9-]{2,})|localhost|[\p{L}\p{N}-]{2,})$/u
  }
];


/* ===============================
 * CORE LOGIC
 * =============================== */

function testBlock(regex, emails, shouldMatch) {
  let pass = 0;
  for (const email of emails) {
    if (regex.test(email) === shouldMatch) pass++;
  }
  return { pass, total: emails.length };
}

function runRegex(regexObj) {
  const detail = { valid: {}, invalid: {} };
  let totalPass = 0;
  let totalTests = 0;

  const start = process.hrtime.bigint();

  for (const block in validEmails) {
    const r = testBlock(regexObj.regex, validEmails[block], true);
    detail.valid[block] = r;
    totalPass += r.pass;
    totalTests += r.total;
  }

  for (const block in invalidEmails) {
    const r = testBlock(regexObj.regex, invalidEmails[block], false);
    detail.invalid[block] = r;
    totalPass += r.pass;
    totalTests += r.total;
  }

  const end = process.hrtime.bigint();
  const totalMs = Number(end - start) / 1e6;
  const msPerEmail = totalMs / totalTests;

  return {
    detail,
    summary: {
      pass: totalPass,
      total: totalTests,
      accuracy: (totalPass / totalTests) * 100,
      totalMs,
      msPerEmail
    }
  };
}

/* ===============================
 * OUTPUT INDIVIDUAL
 * =============================== */

function printRegexReport(name, results) {
  console.log("\n" + "=".repeat(48));
  console.log(`Regex: ${name}`);
  console.log("=".repeat(48));

  for (const section of ["valid", "invalid"]) {
    console.log(`\n${section.toUpperCase()} EMAILS`);
    for (const block in results.detail[section]) {
      const { pass, total } = results.detail[section][block];
      const percent = ((pass / total) * 100).toFixed(1);
      const icon = pass === total ? "✅" : pass === 0 ? "❌" : "⚠️";

      console.log(
        `  ${block.padEnd(22)} ${icon} ${pass}/${total} (${percent}%)`
      );
    }
  }

  console.log(
    `\n🎯 OVERALL: ${results.summary.pass}/${results.summary.total} ` +
    `(${results.summary.accuracy.toFixed(2)}%)`
  );

  console.log(
    `⏱ TIME: ${results.summary.totalMs.toFixed(3)} ms ` +
    `| ${results.summary.msPerEmail.toFixed(5)} ms/email`
  );
}

/* ===============================
 * EXECUÇÃO
 * =============================== */

const finalResults = [];

for (const regexObj of regexes) {
  const results = runRegex(regexObj);
  printRegexReport(regexObj.name, results);

  finalResults.push({
    name: regexObj.name,
    ...results.summary
  });
}

/* ===============================
 * RANKING FINAL (ALINHADO)
 * =============================== */

const ranking = [...finalResults].sort(
  (a, b) => b.accuracy - a.accuracy || a.msPerEmail - b.msPerEmail
);

const nameWidth = Math.max(...ranking.map(r => r.name.length), 10);

console.log("\n" + "=".repeat(nameWidth + 66));
console.log("🏆 REGEX RANKING (BEST → WORST)");
console.log("=".repeat(nameWidth + 66));

console.log(
  "RANK ".padEnd(6) +
  "REGEX".padEnd(nameWidth + 2) +
  "PASS/TOTAL".padEnd(14) +
  "ACC %".padEnd(10) +
  "ms/email".padEnd(12) +
  "TOTAL ms"
);

console.log("-".repeat(nameWidth + 66));

ranking.forEach((r, i) => {
  const medal = i === 0 ? "🥇" : i === 1 ? "🥈" : i === 2 ? "🥉" : " ";
  console.log(
    `${medal} #${String(i + 1).padStart(2, "0")}  ` +
    r.name.padEnd(nameWidth + 2) +
    `${r.pass}/${r.total}`.padEnd(14) +
    r.accuracy.toFixed(2).padEnd(10) +
    r.msPerEmail.toFixed(5).padEnd(12) +
    r.totalMs.toFixed(3)
  );
});
