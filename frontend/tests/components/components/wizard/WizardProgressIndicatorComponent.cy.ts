import WizardProgressIndicatorComponent from '@/components/wizard/WizardProgressIndicatorComponent.vue'
import type { ProgressData } from '@/dto/CommonTypesDto'

describe('<WizardProgressIndicatorComponent />', () => {
  beforeEach(() => {
    cy.fixture('progress.1.json').as('progress1Fixture')
    cy.fixture('progress.2.json').as('progress2Fixture')
  })

  it('steps are displayed horizontally', () => {
    cy.viewport(768, 660)
    cy.get('@progress1Fixture').then((progress:ProgressData[]) => {
      cy.mount(WizardProgressIndicatorComponent, {
        props: { modelValue: progress }
      })
    })

    cy.get('.bx--progress-step')
      .then((elements) =>
        elements.map((_index, el) => el.getBoundingClientRect())
      )
      .as('stepContainers')

    cy.get<DOMRect[]>('@stepContainers').should('have.lengthOf', 4)

    cy.get<DOMRect[]>('@stepContainers').then((stepContainers) => {
      expect(stepContainers[0].top)
        .to.eq(stepContainers[1].top)
        .and.eq(stepContainers[2].top)
        .and.eq(stepContainers[3].top)

      expect(stepContainers[0].left).to.be.lessThan(stepContainers[1].left)
      expect(stepContainers[1].left).to.be.lessThan(stepContainers[2].left)
      expect(stepContainers[2].left).to.be.lessThan(stepContainers[3].left)
    })
  })

  it('steps are displayed vertically', () => {
    cy.viewport(320, 660)
    cy.get('@progress1Fixture').then((progress:ProgressData[]) => {
      cy.mount(WizardProgressIndicatorComponent, {
        props: { modelValue: progress }
      })
    })
    cy.get('.bx--progress-step')
      .then((elements) =>
        elements.map(function () {
          return this.getBoundingClientRect()
        })
      )
      .as('stepContainers')

    cy.get<DOMRect[]>('@stepContainers').should('have.lengthOf', 4)

    cy.get<DOMRect[]>('@stepContainers').then((stepContainers) => {
      expect(stepContainers[0].left)
        .to.eq(stepContainers[1].left)
        .and.eq(stepContainers[2].left)
        .and.eq(stepContainers[3].left)

      expect(stepContainers[0].top).to.be.lessThan(stepContainers[1].top)
      expect(stepContainers[1].top).to.be.lessThan(stepContainers[2].top)
      expect(stepContainers[2].top).to.be.lessThan(stepContainers[3].top)
    })
  })

  it('should render all states', () => {
    cy.viewport(768, 660)
    cy.get('@progress2Fixture').then((progress:ProgressData[]) => {
      cy.mount(WizardProgressIndicatorComponent, {
        props: { modelValue: progress }
      })
    })

    cy.get('.bx--progress-step')
      .then((elements) =>
        elements.map((_index, el) => el.getBoundingClientRect())
      )
      .as('stepContainers')

    cy.get('[data-test="step-0"')
      .should('be.visible')
      .and('have.class', 'bx--progress-step-complete')
      .find('svg')
      .should('be.visible')
      .and('have.class', 'bx--progress-step-icon-complete')

    cy.get('[data-test="step-1"')
      .should('be.visible')
      .and('have.class', 'bx--progress-step-current')
      .find('svg')
      .should('be.visible')
      .and('have.class', 'bx--progress-step-icon-current')

    cy.get('[data-test="step-2"')
      .should('be.visible')
      .and('have.class', 'bx--progress-step-queued')
      .find('svg')
      .should('be.visible')
      .and('have.class', 'bx--progress-step-icon-queued')

    cy.get('[data-test="step-3"')
      .should('be.visible')
      .and('have.class', 'bx--progress-step-error')
      .find('svg')
      .should('be.visible')
      .and('have.class', 'bx--progress-step-icon-error')
  })
})
