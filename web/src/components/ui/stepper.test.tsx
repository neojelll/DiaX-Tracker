import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { describe, expect, it } from 'vitest'
import { Stepper } from '@/components/ui/stepper'

function Harness({ start }: { start: number }) {
  const [value, setValue] = useState(start)
  return (
    <Stepper
      label="Нижняя граница"
      value={value}
      onChange={setValue}
      step={0.1}
      min={3.9}
      max={4.1}
      format={(v) => v.toFixed(1)}
    />
  )
}

describe('Stepper', () => {
  it('steps without float drift and stops at its bounds', async () => {
    render(<Harness start={3.9} />)
    const less = screen.getByRole('button', { name: 'Меньше: Нижняя граница' })
    const more = screen.getByRole('button', { name: 'Больше: Нижняя граница' })
    expect(less).toBeDisabled()
    await userEvent.click(more)
    await userEvent.click(more)
    expect(screen.getByRole('status', { name: 'Нижняя граница' })).toHaveTextContent('4.1')
    expect(more).toBeDisabled()
    await userEvent.click(less)
    expect(screen.getByRole('status', { name: 'Нижняя граница' })).toHaveTextContent('4.0')
  })
})
