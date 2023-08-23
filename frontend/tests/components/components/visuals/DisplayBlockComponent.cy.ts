import DisplayBlockComponent from '@/components/visuals/DisplayBlockComponent.vue'

describe('<DisplayBlockComponent />', () => {
  ;['info', 'success', 'warning', 'error'].forEach((kind: string) => {
    it(`should render the component as ${kind}`, () => {
      cy.mount(DisplayBlockComponent, {
        props: {
          kind,
          title: `Test ${kind}`,
          subtitle: `Test Sub${kind}`
        }
      })
      cy.get('div').should('be.visible')

      cy.get('[data-testid="display-block-wrapper"]')
        .should('have.attr', 'class')
        .and('contain', 'display-block-wrapper')
        .and('contain', `display-block-wrapper-${kind}`)

      cy.get('[data-testid="display-block-icon"]')
        .should('have.attr', 'class')
        .and('contain', 'display-block-icon')
        .and('contain', `display-block-icon-${kind}`)

      cy.get('[data-testid="display-block-icon"]')
        .should('have.attr', 'alt')
        .and('contain', kind)

      cy.get('p').should('be.visible').and('contain', `Test ${kind}`)

      cy.get('span').should('be.visible').and('contain', `Test Sub${kind}`)
    })
  })

  it('should render default slot content', () => {
    cy.mount(DisplayBlockComponent, {
      props: {
        kind: 'info',
        title: 'Test Info',
        subtitle: 'Test SubInfo'
      },
      slots: {
        default: 'Test Default Slot'
      }
    })
    cy.get('div.grouping-08').should('be.visible')
    cy.get('div.grouping-08').should('contain', 'Test Default Slot')
  })
})
