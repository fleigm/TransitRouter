package de.fleigm.transitrouter.feeds;

public enum Status {
  PENDING {
    @Override
    public boolean pending() {
      return true;
    }
  },
  FINISHED {
    @Override
    public boolean finished() {
      return true;
    }
  },
  FAILED {
    @Override
    public boolean failed() {
      return true;
    }
  };

  public boolean pending() {
    return false;
  }

  public boolean failed() {
    return false;
  }

  public boolean finished() {
    return false;
  }


}
