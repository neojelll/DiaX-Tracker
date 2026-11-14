/** A tab that hasn't moved over from Android yet: its title and what will be here. */
export function Placeholder({ title, note }: { title: string; note: string }) {
  return (
    <section className="px-3.5 pt-4">
      <h1 className="text-2xl leading-[1.1] font-medium tracking-[-0.02em]">{title}</h1>
      <p className="mt-3 text-[13.5px] leading-[18px] text-text-secondary">{note}</p>
    </section>
  )
}
