import { Test, TestingModule } from '@nestjs/testing';
import { QualificationController } from './qualification.controller.js';

describe('QualificationController', () => {
  let controller: QualificationController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [QualificationController],
    }).compile();

    controller = module.get<QualificationController>(QualificationController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
